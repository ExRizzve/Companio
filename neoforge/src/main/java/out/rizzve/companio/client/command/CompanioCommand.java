package out.rizzve.companio.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import out.rizzve.companio.Companio;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.skin.MojangProfileService;
import out.rizzve.companio.client.skin.ProfileCompat;

import java.util.concurrent.CompletionException;

public final class CompanioCommand {
    private CompanioCommand() {
    }

    public static void register(
            CompanionController controller,
            MojangProfileService profileService,
            ConfigManager configManager
    ) {
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                Commands.literal("companio")
                        .then(CompanioConfigCommand.build(controller, configManager))
                        .then(Commands.literal("remove")
                                .then(Commands.literal("all").executes(command -> {
                                    controller.remove();
                                    sendSuccess(command.getSource(), Component.translatable("companio.command.removed"));
                                    return 1;
                                }))
                                .then(Commands.argument("number", IntegerArgumentType.integer(1, CompanionController.MAX_COMPANIONS))
                                        .suggests((command, builder) -> CompanioCompleter.companions(controller, builder))
                                        .executes(command -> remove(
                                                command.getSource(),
                                                controller,
                                                IntegerArgumentType.getInteger(command, "number")
                                        ))))
                        .then(Commands.literal("reload").executes(command -> {
                            controller.updateConfig(configManager.load());
                            sendSuccess(command.getSource(), Component.translatable("companio.command.reloaded"));
                            return 1;
                        }))
                        .then(Commands.literal("name")
                                .then(Commands.argument("number", IntegerArgumentType.integer(1, CompanionController.MAX_COMPANIONS))
                                        .suggests((command, builder) -> CompanioCompleter.companions(controller, builder))
                                        .then(Commands.literal("clear").executes(command -> setName(
                                                command.getSource(),
                                                controller,
                                                IntegerArgumentType.getInteger(command, "number"),
                                                ""
                                        )))
                                        .then(Commands.argument("name", StringArgumentType.greedyString()).executes(command -> setName(
                                                command.getSource(),
                                                controller,
                                                IntegerArgumentType.getInteger(command, "number"),
                                                StringArgumentType.getString(command, "name")
                                        )))))
                        .then(Commands.literal("create")
                                .executes(command -> {
                                    if (!controller.summonDefault(Minecraft.getInstance())) {
                                        sendError(command.getSource(), Component.translatable("companio.error.limit_reached"));
                                        return 0;
                                    }
                                    sendSuccess(command.getSource(), Component.translatable("companio.command.summoned_default"));
                                    sendNumber(command.getSource(), controller);
                                    return 1;
                                })
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .suggests((command, builder) -> SharedSuggestionProvider.suggest(
                                                Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                                                        .map(player -> ProfileCompat.name(player.getProfile())),
                                                builder
                                        ))
                                        .executes(command -> createWithPlayer(
                                                StringArgumentType.getString(command, "player"),
                                                command.getSource(),
                                                controller,
                                                profileService,
                                                configManager
                                        ))))
        ));
    }

    private static int createWithPlayer(
            String playerName,
            CommandSourceStack source,
            CompanionController controller,
            MojangProfileService profileService,
            ConfigManager configManager
    ) {
        if (controller.isFull()) {
            sendError(source, Component.translatable("companio.error.limit_reached"));
            return 0;
        }
        sendFeedback(source, format(ChatFormatting.YELLOW, Component.translatable("companio.command.loading", playerName)));

        profileService.find(playerName).whenComplete((profile, error) -> Minecraft.getInstance().execute(() -> {
            if (error != null) {
                Throwable cause = unwrap(error);
                Companio.LOGGER.error("Could not load profile for {}", playerName, cause);
                Component message = cause instanceof MojangProfileService.ProfileException profileError
                        ? profileError.toComponent()
                        : Component.translatable("companio.error.skin_load");
                source.sendFailure(format(ChatFormatting.RED, message));
                return;
            }

            CompanioConfig config = configManager.load().withLastPlayerName(ProfileCompat.name(profile));
            configManager.save(config);
            controller.updateConfig(config);
            if (!controller.summon(profile, Minecraft.getInstance())) {
                sendError(source, Component.translatable("companio.error.limit_reached"));
                return;
            }
            sendSuccess(source, Component.translatable("companio.command.summoned", ProfileCompat.name(profile)));
            sendNumber(source, controller);
        }));
        return 1;
    }

    private static int setName(
            CommandSourceStack source,
            CompanionController controller,
            int number,
            String name
    ) {
        if (name.length() > 32) {
            sendError(source, Component.translatable("companio.error.name_too_long"));
            return 0;
        }
        if (!controller.setName(number, name)) {
            sendError(source, Component.translatable("companio.error.invalid_companion_number", number));
            return 0;
        }

        Component message = name.isBlank()
                ? Component.translatable("companio.command.name_cleared", number)
                : Component.translatable("companio.command.name_set", number, name);
        sendSuccess(source, message);
        return 1;
    }

    private static int remove(
            CommandSourceStack source,
            CompanionController controller,
            int number
    ) {
        if (!controller.remove(number)) {
            sendError(source, Component.translatable("companio.error.invalid_companion_number", number));
            return 0;
        }
        sendSuccess(source, Component.translatable("companio.command.removed_number", number));
        return 1;
    }

    private static void sendNumber(CommandSourceStack source, CompanionController controller) {
        sendSuccess(source, Component.translatable("companio.command.number", controller.size()));
    }

    private static void sendSuccess(CommandSourceStack source, Component message) {
        sendFeedback(source, format(ChatFormatting.GREEN, message));
    }

    private static void sendError(CommandSourceStack source, Component message) {
        source.sendFailure(format(ChatFormatting.RED, message));
    }

    private static void sendFeedback(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }

    private static Component format(ChatFormatting color, Component message) {
        return Component.literal("[C] - ")
                .withStyle(ChatFormatting.DARK_GREEN)
                .append(message.copy().withStyle(color));
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}

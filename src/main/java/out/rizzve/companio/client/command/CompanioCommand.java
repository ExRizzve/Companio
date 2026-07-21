package out.rizzve.companio.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import out.rizzve.companio.Companio;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.skin.MojangProfileService;

import java.util.concurrent.CompletionException;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class CompanioCommand {
    private CompanioCommand() {
    }

    public static void register(
            CompanionController controller,
            MojangProfileService profileService,
            ConfigManager configManager
    ) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
                literal("companio")
                        .then(CompanioConfigCommand.build(controller, configManager))
                        .then(literal("remove")
                                .then(literal("all").executes(command -> {
                                    controller.remove();
                                    sendSuccess(command.getSource(), Component.translatable("companio.command.removed"));
                                    return 1;
                                }))
                                .then(argument("number", IntegerArgumentType.integer(1, CompanionController.MAX_COMPANIONS))
                                        .suggests((command, builder) -> CompanioCompleter.companions(controller, builder))
                                        .executes(command -> remove(
                                                command.getSource(),
                                                controller,
                                                IntegerArgumentType.getInteger(command, "number")
                                        ))))
                        .then(literal("reload").executes(command -> {
                            controller.updateConfig(configManager.load());
                            sendSuccess(command.getSource(), Component.translatable("companio.command.reloaded"));
                            return 1;
                        }))
                        .then(literal("name")
                                .then(argument("number", IntegerArgumentType.integer(1, CompanionController.MAX_COMPANIONS))
                                        .suggests((command, builder) -> CompanioCompleter.companions(controller, builder))
                                        .then(literal("clear").executes(command -> setName(
                                                command.getSource(),
                                                controller,
                                                IntegerArgumentType.getInteger(command, "number"),
                                                ""
                                        )))
                                        .then(argument("name", StringArgumentType.greedyString()).executes(command -> setName(
                                                command.getSource(),
                                                controller,
                                                IntegerArgumentType.getInteger(command, "number"),
                                                StringArgumentType.getString(command, "name")
                                        )))))
                        .then(literal("create")
                                .executes(command -> {
                                    if (!controller.summonDefault(command.getSource().getClient())) {
                                        sendError(command.getSource(), Component.translatable("companio.error.limit_reached"));
                                        return 0;
                                    }
                                    sendSuccess(command.getSource(), Component.translatable("companio.command.summoned_default"));
                                    sendNumber(command.getSource(), controller);
                                    return 1;
                                })
                                .then(argument("player", StringArgumentType.word())
                                        .suggests((command, builder) -> SharedSuggestionProvider.suggest(
                                                command.getSource().getClient().getConnection().getOnlinePlayers().stream()
                                                        .map(player -> player.getProfile().name()),
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
            FabricClientCommandSource source,
            CompanionController controller,
            MojangProfileService profileService,
            ConfigManager configManager
    ) {
        if (controller.isFull()) {
            sendError(source, Component.translatable("companio.error.limit_reached"));
            return 0;
        }
        source.sendFeedback(format(ChatFormatting.YELLOW, Component.translatable("companio.command.loading", playerName)));

        profileService.find(playerName).whenComplete((profile, error) -> source.getClient().execute(() -> {
            if (error != null) {
                Throwable cause = unwrap(error);
                Companio.LOGGER.error("Could not load profile for {}", playerName, cause);
                Component message = cause instanceof MojangProfileService.ProfileException profileError
                        ? profileError.toComponent()
                        : Component.translatable("companio.error.skin_load");
                source.sendError(format(ChatFormatting.RED, message));
                return;
            }

            CompanioConfig config = configManager.load().withLastPlayerName(profile.name());
            configManager.save(config);
            controller.updateConfig(config);
            if (!controller.summon(profile, source.getClient())) {
                sendError(source, Component.translatable("companio.error.limit_reached"));
                return;
            }
            sendSuccess(source, Component.translatable("companio.command.summoned", profile.name()));
            sendNumber(source, controller);
        }));
        return 1;
    }

    private static int setName(
            FabricClientCommandSource source,
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
            FabricClientCommandSource source,
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

    private static void sendNumber(FabricClientCommandSource source, CompanionController controller) {
        sendSuccess(source, Component.translatable("companio.command.number", controller.size()));
    }

    private static void sendSuccess(FabricClientCommandSource source, Component message) {
        source.sendFeedback(format(ChatFormatting.GREEN, message));
    }

    private static void sendError(FabricClientCommandSource source, Component message) {
        source.sendError(format(ChatFormatting.RED, message));
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

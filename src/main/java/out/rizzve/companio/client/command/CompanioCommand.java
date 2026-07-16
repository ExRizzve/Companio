package out.rizzve.companio.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
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
                        .then(literal("remove").executes(command -> {
                            controller.remove();
                            sendSuccess(command.getSource(), Component.translatable("companio.command.removed"));
                            return 1;
                        }))
                        .then(literal("reload").executes(command -> {
                            controller.updateConfig(configManager.load());
                            sendSuccess(command.getSource(), Component.translatable("companio.command.reloaded"));
                            return 1;
                        }))
                        .then(literal("name")
                                .then(literal("clear").executes(command -> setName(
                                        command.getSource(), controller, ""
                                )))
                                .then(argument("name", StringArgumentType.greedyString()).executes(command -> setName(
                                        command.getSource(),
                                        controller,
                                        StringArgumentType.getString(command, "name")
                                ))))
                        .then(literal("event")
                                .executes(command -> triggerEvent(command.getSource(), controller, "random"))
                                .then(argument("type", StringArgumentType.word())
                                        .suggests((command, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"spin", "twirl", "orbit"}, builder
                                        ))
                                        .executes(command -> triggerEvent(
                                                command.getSource(),
                                                controller,
                                                StringArgumentType.getString(command, "type")
                                        ))))
                        .then(literal("create")
                                .executes(command -> {
                                    controller.summonDefault(command.getSource().getClient());
                                    sendSuccess(command.getSource(), Component.translatable("companio.command.summoned_default"));
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
            controller.summon(profile, source.getClient());
            sendSuccess(source, Component.translatable("companio.command.summoned", profile.name()));
        }));
        return 1;
    }

    private static int setName(
            FabricClientCommandSource source,
            CompanionController controller,
            String name
    ) {
        if (name.length() > 32) {
            sendError(source, Component.translatable("companio.error.name_too_long"));
            return 0;
        }
        if (!controller.setName(name)) {
            sendError(source, Component.translatable("companio.error.no_companion"));
            return 0;
        }

        sendSuccess(source, Component.translatable(name.isBlank()
                ? "companio.command.name_cleared"
                : "companio.command.name_set", name));
        return 1;
    }

    private static int triggerEvent(
            FabricClientCommandSource source,
            CompanionController controller,
            String eventName
    ) {
        if (!controller.triggerEvent(eventName)) {
            sendError(source, Component.translatable("companio.error.no_companion"));
            return 0;
        }

        sendSuccess(source, Component.translatable(
                "companio.command.event_started",
                Component.translatable("companio.event." + eventName)
        ));
        return 1;
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

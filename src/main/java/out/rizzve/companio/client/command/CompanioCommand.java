package out.rizzve.companio.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import out.rizzve.companio.client.companion.CompanionActions;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.companion.CompanionHat;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.skin.MojangProfileService;
import out.rizzve.companio.client.skin.ProfileCompat;

import static out.rizzve.companio.client.command.ClientCommandsCompat.argument;
import static out.rizzve.companio.client.command.ClientCommandsCompat.literal;

public final class CompanioCommand {
    private CompanioCommand() {
    }

    public static void register(
            CompanionController controller,
            MojangProfileService profileService,
            ConfigManager configManager
    ) {
        CompanionActions actions = new CompanionActions(controller, configManager, profileService);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
                literal("companio")
                        .then(CompanioConfigCommand.build(controller, configManager))
                        .then(literal("remove")
                                .then(literal("all").executes(command ->
                                        report(command.getSource(), actions.removeAll(command.getSource().getClient()))))
                                .then(argument("number", IntegerArgumentType.integer(1))
                                        .suggests((command, builder) -> CompanioCompleter.companions(controller, builder))
                                        .executes(command -> report(command.getSource(), actions.remove(
                                                IntegerArgumentType.getInteger(command, "number"),
                                                command.getSource().getClient())))))
                        .then(literal("hat")
                                .then(argument("number", IntegerArgumentType.integer(1))
                                        .suggests((command, builder) -> CompanioCompleter.companions(controller, builder))
                                        .then(argument("hat", StringArgumentType.word())
                                                .suggests((command, builder) -> CompanioCompleter.hats(builder))
                                                .executes(command -> setHat(
                                                        command.getSource(),
                                                        actions,
                                                        IntegerArgumentType.getInteger(command, "number"),
                                                        StringArgumentType.getString(command, "hat"))))))
                        .then(literal("reload").executes(command ->
                                report(command.getSource(), actions.reload(command.getSource().getClient()))))
                        .then(literal("name")
                                .then(argument("number", IntegerArgumentType.integer(1))
                                        .suggests((command, builder) -> CompanioCompleter.companions(controller, builder))
                                        .then(literal("clear").executes(command -> report(command.getSource(), actions.setName(
                                                IntegerArgumentType.getInteger(command, "number"),
                                                "",
                                                command.getSource().getClient()))))
                                        .then(argument("name", StringArgumentType.greedyString()).executes(command ->
                                                report(command.getSource(), actions.setName(
                                                        IntegerArgumentType.getInteger(command, "number"),
                                                        StringArgumentType.getString(command, "name"),
                                                        command.getSource().getClient()))))))
                        .then(literal("create")
                                .executes(command -> create(command.getSource(), actions, ""))
                                .then(argument("player", StringArgumentType.word())
                                        .suggests((command, builder) -> SharedSuggestionProvider.suggest(
                                                command.getSource().getClient().getConnection().getOnlinePlayers().stream()
                                                        .map(player -> ProfileCompat.name(player.getProfile())),
                                                builder
                                        ))
                                        .executes(command -> create(
                                                command.getSource(),
                                                actions,
                                                StringArgumentType.getString(command, "player")))))
        ));
    }

    private static int create(FabricClientCommandSource source, CompanionActions actions, String playerName) {
        CompanionActions.Result result = actions.create(playerName, source.getClient());
        report(source, result);
        if (result.success()) {
            sendSuccess(source, Component.translatable("companio.command.number", actions.count()));
        }
        return result.success() ? 1 : 0;
    }

    private static int setHat(
            FabricClientCommandSource source,
            CompanionActions actions,
            int number,
            String hatId
    ) {
        CompanionHat hat = CompanionHat.byId(hatId).orElse(null);
        if (hat == null) {
            sendError(source, Component.translatable("companio.error.unknown_hat", hatId));
            return 0;
        }
        return report(source, actions.setHat(number, hat, source.getClient()));
    }

    private static int report(FabricClientCommandSource source, CompanionActions.Result result) {
        if (result.success()) {
            sendSuccess(source, result.message());
            return 1;
        }
        sendError(source, result.message());
        return 0;
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
}

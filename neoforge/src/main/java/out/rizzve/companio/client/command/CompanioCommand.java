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
import out.rizzve.companio.client.companion.CompanionActions;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.companion.CompanionHat;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.skin.MojangProfileService;
import out.rizzve.companio.client.skin.ProfileCompat;

public final class CompanioCommand {
    private CompanioCommand() {
    }

    public static void register(
            CompanionController controller,
            MojangProfileService profileService,
            ConfigManager configManager
    ) {
        CompanionActions actions = new CompanionActions(controller, configManager, profileService);

        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                Commands.literal("companio")
                        .then(CompanioConfigCommand.build(controller, configManager))
                        .then(Commands.literal("remove")
                                .then(Commands.literal("all").executes(command ->
                                        report(command.getSource(), actions.removeAll(Minecraft.getInstance()))))
                                .then(Commands.argument("number", IntegerArgumentType.integer(1))
                                        .suggests((command, builder) -> CompanioCompleter.companions(controller, builder))
                                        .executes(command -> report(command.getSource(), actions.remove(
                                                IntegerArgumentType.getInteger(command, "number"),
                                                Minecraft.getInstance())))))
                        .then(Commands.literal("hat")
                                .then(Commands.argument("number", IntegerArgumentType.integer(1))
                                        .suggests((command, builder) -> CompanioCompleter.companions(controller, builder))
                                        .then(Commands.argument("hat", StringArgumentType.word())
                                                .suggests((command, builder) -> CompanioCompleter.hats(builder))
                                                .executes(command -> setHat(
                                                        command.getSource(),
                                                        actions,
                                                        IntegerArgumentType.getInteger(command, "number"),
                                                        StringArgumentType.getString(command, "hat"))))))
                        .then(Commands.literal("reload").executes(command ->
                                report(command.getSource(), actions.reload(Minecraft.getInstance()))))
                        .then(Commands.literal("name")
                                .then(Commands.argument("number", IntegerArgumentType.integer(1))
                                        .suggests((command, builder) -> CompanioCompleter.companions(controller, builder))
                                        .then(Commands.literal("clear").executes(command -> report(command.getSource(), actions.setName(
                                                IntegerArgumentType.getInteger(command, "number"),
                                                "",
                                                Minecraft.getInstance()))))
                                        .then(Commands.argument("name", StringArgumentType.greedyString()).executes(command ->
                                                report(command.getSource(), actions.setName(
                                                        IntegerArgumentType.getInteger(command, "number"),
                                                        StringArgumentType.getString(command, "name"),
                                                        Minecraft.getInstance()))))))
                        .then(Commands.literal("create")
                                .executes(command -> create(command.getSource(), actions, ""))
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .suggests((command, builder) -> SharedSuggestionProvider.suggest(
                                                Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                                                        .map(player -> ProfileCompat.name(player.getProfile())),
                                                builder
                                        ))
                                        .executes(command -> create(
                                                command.getSource(),
                                                actions,
                                                StringArgumentType.getString(command, "player")))))
        ));
    }

    private static int create(CommandSourceStack source, CompanionActions actions, String playerName) {
        CompanionActions.Result result = actions.create(playerName, Minecraft.getInstance());
        report(source, result);
        if (result.success()) {
            sendSuccess(source, Component.translatable("companio.command.number", actions.count()));
        }
        return result.success() ? 1 : 0;
    }

    private static int setHat(
            CommandSourceStack source,
            CompanionActions actions,
            int number,
            String hatId
    ) {
        CompanionHat hat = CompanionHat.byId(hatId).orElse(null);
        if (hat == null) {
            sendError(source, Component.translatable("companio.error.unknown_hat", hatId));
            return 0;
        }
        return report(source, actions.setHat(number, hat, Minecraft.getInstance()));
    }

    private static int report(CommandSourceStack source, CompanionActions.Result result) {
        if (result.success()) {
            sendSuccess(source, result.message());
            return 1;
        }
        sendError(source, result.message());
        return 0;
    }

    private static void sendSuccess(CommandSourceStack source, Component message) {
        Component formatted = format(ChatFormatting.GREEN, message);
        source.sendSuccess(() -> formatted, false);
    }

    private static void sendError(CommandSourceStack source, Component message) {
        source.sendFailure(format(ChatFormatting.RED, message));
    }

    private static Component format(ChatFormatting color, Component message) {
        return Component.literal("[C] - ")
                .withStyle(ChatFormatting.DARK_GREEN)
                .append(message.copy().withStyle(color));
    }
}

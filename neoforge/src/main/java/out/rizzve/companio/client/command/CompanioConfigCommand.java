package out.rizzve.companio.client.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.config.ConfigManager;

import java.util.function.UnaryOperator;

public final class CompanioConfigCommand {
    private CompanioConfigCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build(
            CompanionController controller,
            ConfigManager manager
    ) {
        return Commands.literal("config")
                .then(Commands.literal("distance")
                        .then(Commands.argument("blocks", DoubleArgumentType.doubleArg(1.0, 12.0))
                                .suggests((command, builder) -> CompanioCompleter.distances(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withWanderRadius(DoubleArgumentType.getDouble(command, "blocks"))
                                ))))
                .then(Commands.literal("height")
                        .then(Commands.argument("blocks", DoubleArgumentType.doubleArg(1.5, 6.0))
                                .suggests((command, builder) -> CompanioCompleter.heights(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withHoverHeight(DoubleArgumentType.getDouble(command, "blocks"))
                                ))))
                .then(Commands.literal("speed")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.04, 0.2))
                                .suggests((command, builder) -> CompanioCompleter.speeds(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withMaxSpeed(DoubleArgumentType.getDouble(command, "value"))
                                ))))
                .then(Commands.literal("follow-speed")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.25, 0.8))
                                .suggests((command, builder) -> CompanioCompleter.followSpeeds(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withFollowSpeed(DoubleArgumentType.getDouble(command, "value"))
                                ))))
                .then(Commands.literal("smoothness")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
                                .suggests((command, builder) -> CompanioCompleter.levels(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withSmoothness(IntegerArgumentType.getInteger(command, "level"))
                                ))))
                .then(Commands.literal("sharpness")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
                                .suggests((command, builder) -> CompanioCompleter.levels(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withTurnSharpness(IntegerArgumentType.getInteger(command, "level"))
                                ))))
                .then(Commands.literal("reset").executes(command -> {
                    manager.save(CompanioConfig.DEFAULT);
                    controller.updateConfig(CompanioConfig.DEFAULT);
                    send(command.getSource(), "companio.command.config_reset");
                    return 1;
                }));
    }

    private static int update(
            CommandSourceStack source,
            CompanionController controller,
            ConfigManager manager,
            UnaryOperator<CompanioConfig> change
    ) {
        CompanioConfig config = change.apply(manager.load());
        manager.save(config);
        controller.updateConfig(config);
        send(source, "companio.command.config_saved");
        return 1;
    }

    private static void send(CommandSourceStack source, String key) {
        Component message = Component.literal("[C] - ")
                .withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.translatable(key).withStyle(ChatFormatting.GREEN));
        source.sendSuccess(() -> message, false);
    }
}

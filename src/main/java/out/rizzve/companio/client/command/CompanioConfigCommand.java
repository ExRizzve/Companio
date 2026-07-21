package out.rizzve.companio.client.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.config.ConfigManager;

import java.util.function.UnaryOperator;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class CompanioConfigCommand {
    private CompanioConfigCommand() {
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> build(
            CompanionController controller,
            ConfigManager manager
    ) {
        return literal("config")
                .then(literal("distance")
                        .then(argument("blocks", DoubleArgumentType.doubleArg(1.0, 12.0))
                                .suggests((command, builder) -> CompanioCompleter.distances(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withWanderRadius(DoubleArgumentType.getDouble(command, "blocks"))
                                ))))
                .then(literal("height")
                        .then(argument("blocks", DoubleArgumentType.doubleArg(1.5, 6.0))
                                .suggests((command, builder) -> CompanioCompleter.heights(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withHoverHeight(DoubleArgumentType.getDouble(command, "blocks"))
                                ))))
                .then(literal("speed")
                        .then(argument("value", DoubleArgumentType.doubleArg(0.04, 0.2))
                                .suggests((command, builder) -> CompanioCompleter.speeds(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withMaxSpeed(DoubleArgumentType.getDouble(command, "value"))
                                ))))
                .then(literal("follow-speed")
                        .then(argument("value", DoubleArgumentType.doubleArg(0.25, 0.8))
                                .suggests((command, builder) -> CompanioCompleter.followSpeeds(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withFollowSpeed(DoubleArgumentType.getDouble(command, "value"))
                                ))))
                .then(literal("smoothness")
                        .then(argument("level", IntegerArgumentType.integer(1, 10))
                                .suggests((command, builder) -> CompanioCompleter.levels(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withSmoothness(IntegerArgumentType.getInteger(command, "level"))
                                ))))
                .then(literal("sharpness")
                        .then(argument("level", IntegerArgumentType.integer(1, 10))
                                .suggests((command, builder) -> CompanioCompleter.levels(builder))
                                .executes(command -> update(
                                        command.getSource(), controller, manager,
                                        config -> config.withTurnSharpness(IntegerArgumentType.getInteger(command, "level"))
                                ))))
                .then(literal("reset").executes(command -> {
                    manager.save(CompanioConfig.DEFAULT);
                    controller.updateConfig(CompanioConfig.DEFAULT);
                    send(command.getSource(), "companio.command.config_reset");
                    return 1;
                }));
    }

    private static int update(
            FabricClientCommandSource source,
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

    private static void send(FabricClientCommandSource source, String key) {
        source.sendFeedback(Component.literal("[C] - ")
                .withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.translatable(key).withStyle(ChatFormatting.GREEN)));
    }
}

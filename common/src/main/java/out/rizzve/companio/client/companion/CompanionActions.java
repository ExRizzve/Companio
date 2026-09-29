package out.rizzve.companio.client.companion;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.config.CompanionSlot;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.skin.MojangProfileService;

import java.util.ArrayList;
import java.util.List;

public final class CompanionActions {
    public record Result(boolean success, Component message) {
    }

    private final CompanionController controller;
    private final ConfigManager configManager;
    private final MojangProfileService profileService;

    public CompanionActions(
            CompanionController controller,
            ConfigManager configManager,
            MojangProfileService profileService
    ) {
        this.controller = controller;
        this.configManager = configManager;
        this.profileService = profileService;
    }

    public Result create(String playerName, Minecraft client) {
        CompanioConfig config = configManager.load();
        if (config.companions().size() >= CompanionController.MAX_COMPANIONS) {
            return failure(Component.translatable("companio.error.limit_reached",
                    CompanionController.MAX_COMPANIONS));
        }

        apply(config.withCompanionAdded(CompanionSlot.EMPTY.withPlayerName(playerName)), client);
        Component message = playerName.isEmpty()
                ? Component.translatable("companio.command.summoned_default")
                : Component.translatable("companio.command.summoned", playerName);
        return new Result(true, message);
    }

    public Result setHat(int number, CompanionHat hat, Minecraft client) {
        CompanioConfig config = configManager.load();
        int index = slotOf(config, number);
        if (index < 0) {
            return invalidNumber(number);
        }

        apply(config.withCompanion(index, config.companions().get(index).withHat(hat)), client);
        Component message = hat == CompanionHat.NONE
                ? Component.translatable("companio.command.hat_cleared", number)
                : Component.translatable("companio.command.hat_set", number,
                        Component.translatable(hat.translationKey()));
        return new Result(true, message);
    }

    public Result setName(int number, String name, Minecraft client) {
        if (name.length() > CompanionSlot.MAX_NAME_LENGTH) {
            return failure(Component.translatable("companio.error.name_too_long"));
        }
        CompanioConfig config = configManager.load();
        int index = slotOf(config, number);
        if (index < 0) {
            return invalidNumber(number);
        }

        apply(config.withCompanion(index, config.companions().get(index).withName(name)), client);
        Component message = name.isBlank()
                ? Component.translatable("companio.command.name_cleared", number)
                : Component.translatable("companio.command.name_set", number, name);
        return new Result(true, message);
    }

    public Result remove(int number, Minecraft client) {
        CompanioConfig config = configManager.load();
        int index = slotOf(config, number);
        if (index < 0) {
            return invalidNumber(number);
        }

        apply(config.withCompanionRemoved(index), client);
        return new Result(true, Component.translatable("companio.command.removed_number", number));
    }

    public Result removeAll(Minecraft client) {
        apply(configManager.load().withCompanions(List.of()), client);
        return new Result(true, Component.translatable("companio.command.removed"));
    }

    public Result reload(Minecraft client) {
        controller.sync(configManager.load(), client, profileService, configManager);
        return new Result(true, Component.translatable("companio.command.reloaded"));
    }

    public int count() {
        return controller.size();
    }

    private void apply(CompanioConfig config, Minecraft client) {
        configManager.save(config);
        controller.sync(config, client, profileService, configManager);
    }

    private static int slotOf(CompanioConfig config, int number) {
        return number >= 1 && number <= config.companions().size() ? number - 1 : -1;
    }

    private static Result invalidNumber(int number) {
        return failure(Component.translatable("companio.error.invalid_companion_number", number));
    }

    private static Result failure(Component message) {
        return new Result(false, message);
    }
}

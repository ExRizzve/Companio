package out.rizzve.companio.client.companion;

import net.minecraft.client.Minecraft;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.skin.MojangProfileService;

public final class CompanionBootstrap {
    private final CompanionController controller;
    private final ConfigManager configManager;
    private final MojangProfileService profileService;
    private boolean restored;

    public CompanionBootstrap(
            CompanionController controller,
            ConfigManager configManager,
            MojangProfileService profileService
    ) {
        this.controller = controller;
        this.configManager = configManager;
        this.profileService = profileService;
    }

    public void tick(Minecraft client) {
        if (client.player == null) {
            restored = false;
        } else if (!restored) {
            restored = true;
            controller.sync(configManager.load(), client, profileService, configManager);
        }
        controller.tick(client);
    }
}

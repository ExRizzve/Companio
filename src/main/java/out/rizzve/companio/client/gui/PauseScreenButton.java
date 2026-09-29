package out.rizzve.companio.client.gui;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import out.rizzve.companio.client.companion.CompanionController;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.skin.MojangProfileService;

public final class PauseScreenButton {
    private static final int WIDTH = 110;
    private static final int HEIGHT = 20;
    private static final int MARGIN = 8;

    private PauseScreenButton() {
    }

    public static void register(
            CompanionController controller,
            ConfigManager configManager,
            MojangProfileService profileService
    ) {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof PauseScreen)) {
                return;
            }
            FabricScreens.widgets(screen).add(Button.builder(
                            CompanionsScreen.TITLE,
                            button -> ScreenCompat.setScreen(client,
                                    new CompanionsScreen(screen, controller, configManager, profileService)))
                    .bounds(MARGIN, scaledHeight - HEIGHT - MARGIN, WIDTH, HEIGHT)
                    .build());
        });
    }
}

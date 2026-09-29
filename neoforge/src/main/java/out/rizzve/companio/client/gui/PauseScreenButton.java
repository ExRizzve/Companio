package out.rizzve.companio.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
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
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Init.Post event) -> {
            Screen screen = event.getScreen();
            if (!(screen instanceof PauseScreen)) {
                return;
            }
            event.addListener(Button.builder(
                            CompanionsScreen.TITLE,
                            button -> ScreenCompat.setScreen(Minecraft.getInstance(),
                                    new CompanionsScreen(screen, controller, configManager, profileService)))
                    .bounds(MARGIN, screen.height - HEIGHT - MARGIN, WIDTH, HEIGHT)
                    .build());
        });
    }
}

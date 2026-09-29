package out.rizzve.companio.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenCompat {
    private ScreenCompat() {
    }

    public static void setScreen(Minecraft client, Screen screen) {
        client.setScreenAndShow(screen);
    }
}
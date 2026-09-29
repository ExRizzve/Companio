package out.rizzve.companio.client.gui;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

final class FabricScreens {
    private FabricScreens() {
    }

    static List<AbstractWidget> widgets(Screen screen) {
        return Screens.getWidgets(screen);
    }
}
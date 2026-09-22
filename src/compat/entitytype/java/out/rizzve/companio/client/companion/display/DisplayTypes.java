package out.rizzve.companio.client.companion.display;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;

final class DisplayTypes {
    private DisplayTypes() {
    }

    static EntityType<Display.ItemDisplay> itemDisplay() {
        return EntityType.ITEM_DISPLAY;
    }

    static EntityType<Display.TextDisplay> textDisplay() {
        return EntityType.TEXT_DISPLAY;
    }
}

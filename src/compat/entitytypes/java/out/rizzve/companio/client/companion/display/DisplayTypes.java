package out.rizzve.companio.client.companion.display;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

final class DisplayTypes {
    private DisplayTypes() {
    }

    static EntityType<Display.ItemDisplay> itemDisplay() {
        return EntityTypes.ITEM_DISPLAY;
    }

    static EntityType<Display.TextDisplay> textDisplay() {
        return EntityTypes.TEXT_DISPLAY;
    }
}

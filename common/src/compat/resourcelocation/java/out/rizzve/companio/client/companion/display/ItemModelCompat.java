package out.rizzve.companio.client.companion.display;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

final class ItemModelCompat {
    private ItemModelCompat() {
    }

    static void apply(ItemStack stack, String namespace, String path) {
        stack.set(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
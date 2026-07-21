package out.rizzve.companio.client.companion.display;

import com.mojang.authlib.GameProfile;
import com.mojang.math.Transformation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicInteger;

public final class CompanionDisplay {
    private static final AtomicInteger ENTITY_IDS = new AtomicInteger(-10_000);
    private static final Transformation HEAD_SCALE = new Transformation(
            new Vector3f(), new Quaternionf(), new Vector3f(1.05F), new Quaternionf());
    private Display.ItemDisplay head;
    private Display.TextDisplay name;
    private ClientLevel level;
    private String customName;

    public void spawn(ClientLevel level, Vec3 position, float yaw, GameProfile profile) {
        discard();
        this.level = level;
        ItemStack item = new ItemStack(Items.PLAYER_HEAD);
        if (profile != null) item.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
        head = new Display.ItemDisplay(EntityTypes.ITEM_DISPLAY, level);
        head.setId(ENTITY_IDS.getAndDecrement());
        head.setItemStack(item);
        head.setItemTransform(ItemDisplayContext.FIXED);
        head.setTransformation(HEAD_SCALE);
        head.setViewRange(1.5F);
        head.setShadowRadius(0.18F);
        head.setShadowStrength(0.35F);
        head.setPosRotInterpolationDuration(3);
        move(position, yaw);
        level.addEntity(head);
        refreshName();
    }

    public boolean isAvailableIn(ClientLevel currentLevel) {
        return head != null && !head.isRemoved() && level == currentLevel;
    }

    public Vec3 position() {
        return head == null ? null : head.position();
    }

    public void move(Vec3 position, float yaw) {
        if (head == null) return;
        head.setPos(position);
        head.setYRot(yaw);
        head.setXRot(0.0F);
        if (name != null) name.setPos(position.add(0.0, 0.72, 0.0));
    }

    public void setName(String customName) {
        this.customName = customName;
        refreshName();
    }

    public void discard() {
        discardEntity(head);
        discardEntity(name);
        head = null;
        name = null;
        level = null;
    }

    private void refreshName() {
        discardEntity(name);
        name = null;
        if (customName == null || customName.isBlank() || head == null || level == null) return;
        name = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, level);
        name.setId(ENTITY_IDS.getAndDecrement());
        name.setText(Component.literal(customName));
        name.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        name.setFlags(Display.TextDisplay.FLAG_SHADOW);
        name.setBackgroundColor(0x40000000);
        name.setViewRange(1.5F);
        name.setPosRotInterpolationDuration(3);
        name.setPos(head.position().add(0.0, 0.72, 0.0));
        level.addEntity(name);
    }

    private static void discardEntity(Entity entity) {
        if (entity != null && !entity.isRemoved()) entity.remove(Entity.RemovalReason.DISCARDED);
    }
}

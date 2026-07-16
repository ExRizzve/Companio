package out.rizzve.companio.client.companion;

import com.mojang.authlib.GameProfile;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.companion.animation.CompanionAnimation;
import out.rizzve.companio.client.companion.animation.OrbitAnimation;
import out.rizzve.companio.client.companion.animation.SpinAnimation;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class CompanionController {
    private static final AtomicInteger ENTITY_IDS = new AtomicInteger(-10_000);
    private static final Transformation HEAD_SCALE = new Transformation(
            new Vector3f(), new Quaternionf(), new Vector3f(1.05F), new Quaternionf()
    );

    private CompanioConfig config;
    private GameProfile profile;
    private boolean active;
    private Display.ItemDisplay display;
    private Display.TextDisplay nameDisplay;
    private ClientLevel level;
    private String companionName;
    private Vec3 velocity = Vec3.ZERO;
    private Vec3 target = Vec3.ZERO;
    private Vec3 ownerAnchor = Vec3.ZERO;
    private int retargetTicks;
    private int age;
    private float yaw;
    private Behavior behavior = Behavior.WANDER;
    private int behaviorTicks;
    private CompanionAnimation animation;
    private CompanionAnimation.Frame animationFrame;

    public CompanionController(CompanioConfig config) {
        this.config = config;
    }

    public void summon(GameProfile profile, Minecraft client) {
        remove();
        this.profile = profile;
        active = true;
        spawn(client);
    }

    public void summonDefault(Minecraft client) {
        remove();
        active = true;
        spawn(client);
    }

    public void updateConfig(CompanioConfig config) {
        this.config = config;
    }

    public boolean setName(String name) {
        if (!active) {
            return false;
        }
        companionName = name;
        refreshNameDisplay();
        return true;
    }

    public boolean triggerEvent(String eventName) {
        if (!active || display == null) {
            return false;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        switch (eventName) {
            case "spin" -> animation = new SpinAnimation(random.nextBoolean() ? 360.0F : -360.0F);
            case "twirl" -> animation = new SpinAnimation(random.nextBoolean() ? 720.0F : -720.0F);
            case "orbit" -> animation = new OrbitAnimation(
                    display.position(), ownerAnchor, random.nextBoolean() ? 1.0 : -1.0, 180
            );
            case "random" -> {
                int choice = random.nextInt(3);
                return triggerEvent(choice == 0 ? "spin" : choice == 1 ? "twirl" : "orbit");
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    public void remove() {
        if (display != null && !display.isRemoved()) {
            display.remove(Entity.RemovalReason.DISCARDED);
        }
        discardNameDisplay();
        display = null;
        level = null;
        profile = null;
        active = false;
        velocity = Vec3.ZERO;
    }

    public void tick(Minecraft client) {
        if (!active || client.player == null || client.level == null) {
            if (client.level == null) {
                discardDisplay();
            }
            return;
        }

        if (display == null || display.isRemoved() || level != client.level) {
            discardDisplay();
            spawn(client);
        }

        updateMovement(client.player);
    }

    private void spawn(Minecraft client) {
        if (client.player == null || client.level == null || !active) {
            return;
        }

        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        if (profile != null) {
            head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
        }

        display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, client.level);
        display.setId(ENTITY_IDS.getAndDecrement());
        display.setItemStack(head);
        display.setItemTransform(ItemDisplayContext.FIXED);
        display.setTransformation(HEAD_SCALE);
        display.setViewRange(1.5F);
        display.setShadowRadius(0.18F);
        display.setShadowStrength(0.35F);
        display.setPosRotInterpolationDuration(3);
        display.setPos(client.player.position().add(1.5, config.hoverHeight(), 0.0));
        client.level.addEntity(display);

        level = client.level;
        ownerAnchor = client.player.position().add(0.0, config.hoverHeight(), 0.0);
        velocity = Vec3.ZERO;
        retargetTicks = 0;
        age = 0;
        yaw = client.player.getYRot();
        behaviorTicks = 0;
        animation = null;
        animationFrame = null;
        display.setYRot(yaw);
        refreshNameDisplay();
    }

    private void updateMovement(LocalPlayer owner) {
        age++;
        Vec3 ownerCenter = owner.position().add(0.0, config.hoverHeight(), 0.0);
        ownerAnchor = ownerCenter;
        double distanceFromOwner = display.position().distanceTo(owner.position());
        double followRadius = Math.min(config.wanderRadius() + 2.0, config.maxDistance());
        boolean catchingUp = distanceFromOwner > followRadius;

        if (catchingUp) {
            target = ownerCenter;
            retargetTicks = 0;
            behaviorTicks = 0;
            animation = null;
            animationFrame = null;
        } else if (animation != null) {
            animationFrame = animation.tick(display.position(), ownerCenter);
            target = animationFrame.target();
            if (animation.isComplete()) {
                animation = null;
                behaviorTicks = 0;
            }
        } else {
            animationFrame = null;
            updateBehavior(ownerCenter);
        }

        Vec3 smoothTarget = target.add(0.0, Math.sin(age * 0.09) * 0.08, 0.0);
        Vec3 desiredDirection = smoothTarget.subtract(display.position());
        double movementSpeed = catchingUp
                ? Math.max(config.maxSpeed() * 2.8, 0.26)
                : config.maxSpeed();
        double acceleration = catchingUp
                ? Math.max(config.acceleration() * 2.5, 0.035)
                : config.acceleration();
        Vec3 desiredVelocity = desiredDirection.lengthSqr() < 0.01
                ? Vec3.ZERO
                : desiredDirection.normalize().scale(movementSpeed);

        velocity = approach(velocity, desiredVelocity, acceleration).scale(0.965);
        display.setPos(display.position().add(velocity));
        updateNamePosition();

        Vec3 lookAt = age % 100 < 70 ? owner.position() : display.position().add(velocity);
        face(lookAt);
    }

    private void updateBehavior(Vec3 center) {
        if (--behaviorTicks <= 0) {
            chooseBehavior(center);
        }

        if (behavior == Behavior.WANDER && (--retargetTicks <= 0 || display.position().distanceToSqr(target) < 0.4)) {
            chooseWanderTarget(center);
        }
    }

    private void chooseBehavior(Vec3 center) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int choice = random.nextInt(100);

        if (choice < 42) {
            behavior = Behavior.WANDER;
            behaviorTicks = random.nextInt(100, 201);
            retargetTicks = 0;
        } else if (choice < 70) {
            behavior = Behavior.WATCH;
            behaviorTicks = 180;
            animation = new OrbitAnimation(
                    display.position(), center, random.nextBoolean() ? 1.0 : -1.0, behaviorTicks
            );
        } else if (choice < 88) {
            behavior = Behavior.WATCH;
            behaviorTicks = random.nextInt(70, 141);
            double side = random.nextBoolean() ? 1.0 : -1.0;
            target = center.add(side * 1.4, 0.0, 0.8);
        } else {
            behavior = Behavior.WATCH;
            behaviorTicks = 72;
            animation = new SpinAnimation(random.nextBoolean() ? 360.0F : -360.0F);
        }
    }

    private void chooseWanderTarget(Vec3 center) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble(Math.PI * 2.0);
        double radius = random.nextDouble(1.5, config.wanderRadius());
        target = center.add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
        retargetTicks = random.nextInt(70, 151);
    }

    private void face(Vec3 point) {
        if (animationFrame != null && animationFrame.overrideYaw()) {
            yaw += animationFrame.yawStep();
            display.setYRot(yaw);
            display.setXRot(0.0F);
            return;
        }

        Vec3 direction = point.subtract(display.position());
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float difference = wrapDegrees(targetYaw - yaw);
        yaw += Math.clamp(difference, -2.5F, 2.5F);
        display.setYRot(yaw);
        display.setXRot(0.0F);
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        } else if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    private static Vec3 approach(Vec3 current, Vec3 target, double maxChange) {
        Vec3 difference = target.subtract(current);
        if (difference.lengthSqr() <= maxChange * maxChange) {
            return target;
        }
        return current.add(difference.normalize().scale(maxChange));
    }

    private void discardDisplay() {
        if (display != null && !display.isRemoved()) {
            display.remove(Entity.RemovalReason.DISCARDED);
        }
        display = null;
        discardNameDisplay();
        level = null;
    }

    private void refreshNameDisplay() {
        discardNameDisplay();
        if (companionName == null || companionName.isBlank() || display == null || level == null) {
            return;
        }

        nameDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
        nameDisplay.setId(ENTITY_IDS.getAndDecrement());
        nameDisplay.setText(Component.literal(companionName));
        nameDisplay.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        nameDisplay.setFlags(Display.TextDisplay.FLAG_SHADOW);
        nameDisplay.setBackgroundColor(0x40000000);
        nameDisplay.setViewRange(1.5F);
        nameDisplay.setPosRotInterpolationDuration(3);
        updateNamePosition();
        level.addEntity(nameDisplay);
    }

    private void updateNamePosition() {
        if (nameDisplay != null && display != null) {
            nameDisplay.setPos(display.position().add(0.0, 0.72, 0.0));
        }
    }

    private void discardNameDisplay() {
        if (nameDisplay != null && !nameDisplay.isRemoved()) {
            nameDisplay.remove(Entity.RemovalReason.DISCARDED);
        }
        nameDisplay = null;
    }

    private enum Behavior {
        WANDER,
        WATCH
    }
}

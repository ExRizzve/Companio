package out.rizzve.companio.client.companion.animation;

import net.minecraft.world.phys.Vec3;

public final class OrbitAnimation implements CompanionAnimation {
    private static final double RADIUS = 2.8;
    private static final double ANGULAR_SPEED = 0.025;

    private final double direction;
    private double angle;
    private int remainingTicks;

    public OrbitAnimation(Vec3 position, Vec3 ownerCenter, double direction, int duration) {
        this.direction = Math.copySign(1.0, direction);
        angle = Math.atan2(position.z - ownerCenter.z, position.x - ownerCenter.x);
        remainingTicks = duration;
    }

    @Override
    public Frame tick(Vec3 position, Vec3 ownerCenter) {
        angle += ANGULAR_SPEED * direction;
        remainingTicks--;
        Vec3 target = ownerCenter.add(Math.cos(angle) * RADIUS, 0.0, Math.sin(angle) * RADIUS);
        return new Frame(target, 0.0F, false);
    }

    @Override
    public boolean isComplete() {
        return remainingTicks <= 0;
    }
}

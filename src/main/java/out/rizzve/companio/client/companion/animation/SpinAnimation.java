package out.rizzve.companio.client.companion.animation;

import net.minecraft.world.phys.Vec3;

public final class SpinAnimation implements CompanionAnimation {
    private static final float DEGREES_PER_TICK = 5.0F;

    private float remainingDegrees;

    public SpinAnimation(float degrees) {
        remainingDegrees = degrees;
    }

    @Override
    public Frame tick(Vec3 position, Vec3 ownerCenter) {
        float step = Math.copySign(Math.min(DEGREES_PER_TICK, Math.abs(remainingDegrees)), remainingDegrees);
        remainingDegrees -= step;
        return new Frame(position, step, true);
    }

    @Override
    public boolean isComplete() {
        return remainingDegrees == 0.0F;
    }
}

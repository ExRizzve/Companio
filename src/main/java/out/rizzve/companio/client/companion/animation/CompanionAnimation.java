package out.rizzve.companio.client.companion.animation;

import net.minecraft.world.phys.Vec3;

public interface CompanionAnimation {
    Frame tick(Vec3 position, Vec3 ownerCenter);

    boolean isComplete();

    record Frame(Vec3 target, float yawStep, boolean overrideYaw) {
    }
}

package out.rizzve.companio.client.companion.movement;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import out.rizzve.companio.client.config.CompanioConfig;

import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

public final class CompanionFlight {
    private static final double ARRIVAL_DISTANCE_SQUARED = 0.16;
    private static final double COLLISION_SIZE = 0.72;
    private static final double COLLISION_STEP = 0.12;
    private static final double PERSONAL_SPACE = 1.15;
    private static final double SEPARATION_STRENGTH = 0.045;
    private static final int TARGET_ATTEMPTS = 12;
    private static final double MINIMUM_TURN_SPEED_SQUARED = 0.0004;

    private Vec3 velocity = Vec3.ZERO;
    private Vec3 targetOffset = Vec3.ZERO;
    private int retargetTicks;
    private float yaw;

    public void reset(float initialYaw, LocalPlayer owner, CompanioConfig config) {
        velocity = Vec3.ZERO;
        yaw = initialYaw;
        chooseTarget(owner, config);
    }

    public FlightState tick(
            Vec3 position,
            LocalPlayer owner,
            CompanioConfig config,
            List<Vec3> companionPositions
    ) {
        Vec3 ownerCenter = owner.position().add(0.0, config.hoverHeight(), 0.0);
        Vec3 target = ownerCenter.add(targetOffset);
        Vec3 difference = target.subtract(position);
        double ownerDistanceSquared = position.distanceToSqr(ownerCenter);
        double fastFollowDistance = Math.min(
                config.maxDistance(),
                Math.max(5.0, config.wanderRadius() * 1.65 + 1.5)
        );
        boolean catchingUp = ownerDistanceSquared > fastFollowDistance * fastFollowDistance;

        if (catchingUp) {
            target = ownerCenter;
            difference = target.subtract(position);
            retargetTicks = 0;
        } else if (--retargetTicks <= 0 || difference.lengthSqr() <= ARRIVAL_DISTANCE_SQUARED) {
            chooseTarget(owner, config);
            target = ownerCenter.add(targetOffset);
            difference = target.subtract(position);
        }

        double speed = catchingUp ? Math.max(config.maxSpeed() * 3.0, config.followSpeed()) : config.maxSpeed();
        double acceleration = catchingUp
                ? Math.max(config.acceleration() * 4.0, 0.055)
                : config.acceleration();
        double distance = difference.length();
        Vec3 desiredVelocity = distance < 0.05
                ? Vec3.ZERO
                : difference.scale(1.0 / distance).scale(Math.min(speed, distance * 0.22));
        desiredVelocity = desiredVelocity.add(separation(position, companionPositions));

        velocity = approach(velocity, desiredVelocity, acceleration).scale(0.97);
        Vec3 nextPosition = moveWithoutClipping(position, owner);
        double horizontalSpeedSquared = velocity.x * velocity.x + velocity.z * velocity.z;
        if (horizontalSpeedSquared >= MINIMUM_TURN_SPEED_SQUARED) {
            float targetYaw = SmoothRotation.fromDirection(velocity.x, velocity.z, yaw);
            yaw = SmoothRotation.approach(yaw, targetYaw, config.turnSpeed());
        }
        return new FlightState(nextPosition, yaw);
    }

    public Vec3 initialPosition(LocalPlayer owner, CompanioConfig config) {
        return owner.position().add(0.0, config.hoverHeight(), 0.0).add(targetOffset);
    }

    private void chooseTarget(LocalPlayer owner, CompanioConfig config) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Vec3 ownerCenter = owner.position().add(0.0, config.hoverHeight(), 0.0);
        double desiredRadius = Math.max(4.5, config.wanderRadius() * 1.65);
        double maximumRadius = Math.max(2.25, Math.min(config.maxDistance() - 1.0, desiredRadius));

        for (int attempt = 0; attempt < TARGET_ATTEMPTS; attempt++) {
            double angle = random.nextDouble(Math.PI * 2.0);
            double radius = random.nextDouble(1.5, maximumRadius);
            Vec3 candidateOffset = new Vec3(
                    Math.cos(angle) * radius,
                    random.nextDouble(-1.0, 1.01),
                    Math.sin(angle) * radius
            );
            if (isFree(owner, ownerCenter.add(candidateOffset))) {
                targetOffset = candidateOffset;
                retargetTicks = random.nextInt(90, 201);
                return;
            }
        }

        targetOffset = new Vec3(0.0, 1.5, 0.0);
        retargetTicks = 20;
    }

    private Vec3 moveWithoutClipping(Vec3 position, LocalPlayer owner) {
        int count = Math.max(1, (int) Math.ceil(velocity.length() / COLLISION_STEP));
        Vec3 step = velocity.scale(1.0 / count);
        Vec3 current = position;
        boolean blocked = false;

        for (int i = 0; i < count; i++) {
            Vec3 next = current.add(step);
            if (isFree(owner, next)) {
                current = next;
                continue;
            }

            Vec3 moved = slide(current, step, owner);
            if (moved.equals(current)) {
                blocked = true;
                break;
            }
            current = moved;
        }

        if (blocked) {
            velocity = velocity.scale(0.2);
            retargetTicks = 0;
        }
        return current;
    }

    private static Vec3 slide(Vec3 position, Vec3 step, LocalPlayer owner) {
        Vec3 current = position;
        Vec3 x = current.add(step.x, 0.0, 0.0);
        if (isFree(owner, x)) {
            current = x;
        }
        Vec3 y = current.add(0.0, step.y, 0.0);
        if (isFree(owner, y)) {
            current = y;
        }
        Vec3 z = current.add(0.0, 0.0, step.z);
        if (isFree(owner, z)) {
            current = z;
        }
        return current;
    }

    private static Vec3 separation(Vec3 position, List<Vec3> companionPositions) {
        Vec3 force = Vec3.ZERO;
        for (Vec3 other : companionPositions) {
            Vec3 difference = position.subtract(other);
            double distance = difference.length();
            if (distance >= PERSONAL_SPACE) {
                continue;
            }
            if (distance < 1.0E-4) {
                double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2.0);
                force = force.add(Math.cos(angle) * SEPARATION_STRENGTH, 0.0, Math.sin(angle) * SEPARATION_STRENGTH);
                continue;
            }
            double strength = (PERSONAL_SPACE - distance) / PERSONAL_SPACE * SEPARATION_STRENGTH;
            force = force.add(difference.scale(strength / distance));
        }
        return force;
    }

    private static boolean isFree(LocalPlayer owner, Vec3 position) {
        return owner.level().noCollision(AABB.ofSize(
                position,
                COLLISION_SIZE,
                COLLISION_SIZE,
                COLLISION_SIZE
        ));
    }

    private static Vec3 approach(Vec3 current, Vec3 target, double maximumChange) {
        Vec3 difference = target.subtract(current);
        if (difference.lengthSqr() <= maximumChange * maximumChange) {
            return target;
        }
        return current.add(difference.normalize().scale(maximumChange));
    }
}

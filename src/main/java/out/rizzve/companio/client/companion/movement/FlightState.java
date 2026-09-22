package out.rizzve.companio.client.companion.movement;

import net.minecraft.world.phys.Vec3;

public record FlightState(Vec3 position, float yaw, boolean teleported) {
    public FlightState(Vec3 position, float yaw) {
        this(position, yaw, false);
    }
}

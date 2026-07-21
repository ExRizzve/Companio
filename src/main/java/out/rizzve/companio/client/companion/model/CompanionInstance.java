package out.rizzve.companio.client.companion.model;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import out.rizzve.companio.client.companion.display.CompanionDisplay;
import out.rizzve.companio.client.companion.movement.CompanionFlight;
import out.rizzve.companio.client.companion.movement.FlightState;
import out.rizzve.companio.client.config.CompanioConfig;

import java.util.List;

public final class CompanionInstance {
    private final GameProfile profile;
    private final CompanionDisplay display = new CompanionDisplay();
    private final CompanionFlight flight = new CompanionFlight();

    public CompanionInstance(GameProfile profile) {
        this.profile = profile;
    }

    public void tick(Minecraft client, CompanioConfig config, List<Vec3> positions) {
        if (client.player == null || client.level == null) {
            display.discard();
            return;
        }
        if (!display.isAvailableIn(client.level)) {
            spawn(client, config);
        }
        FlightState state = flight.tick(display.position(), client.player, config, positions);
        display.move(state.position(), state.yaw());
    }

    public Vec3 position() {
        return display.position();
    }

    public void setName(String name) {
        display.setName(name);
    }

    public void discard() {
        display.discard();
    }

    private void spawn(Minecraft client, CompanioConfig config) {
        float yaw = client.player.getYRot();
        flight.reset(yaw, client.player, config);
        display.spawn(client.level, flight.initialPosition(client.player, config), yaw, profile);
    }
}

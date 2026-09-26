package out.rizzve.companio.client.companion;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import out.rizzve.companio.client.companion.model.CompanionInstance;
import out.rizzve.companio.client.config.CompanioConfig;

import java.util.ArrayList;
import java.util.List;

public final class CompanionController {
    public static final int MAX_COMPANIONS = 6;

    private final List<CompanionInstance> companions = new ArrayList<>();
    private final List<Vec3> neighbourPositions = new ArrayList<>(MAX_COMPANIONS - 1);
    private CompanioConfig config;

    public CompanionController(CompanioConfig config) {
        this.config = config;
    }

    public boolean summon(GameProfile profile, Minecraft client) {
        return add(new CompanionInstance(profile), client);
    }

    public boolean summonDefault(Minecraft client) {
        return add(new CompanionInstance(null), client);
    }

    public boolean isFull() {
        return companions.size() >= MAX_COMPANIONS;
    }

    public int size() {
        return companions.size();
    }

    public void updateConfig(CompanioConfig config) {
        this.config = config;
    }

    public boolean setName(int number, String name) {
        if (number < 1 || number > companions.size()) {
            return false;
        }
        companions.get(number - 1).setName(name);
        return true;
    }

    public void remove() {
        companions.forEach(CompanionInstance::discard);
        companions.clear();
    }

    public boolean remove(int number) {
        if (number < 1 || number > companions.size()) {
            return false;
        }
        companions.remove(number - 1).discard();
        return true;
    }

    public void tick(Minecraft client) {
        if (companions.isEmpty()) {
            return;
        }
        for (CompanionInstance companion : companions) {
            neighbourPositions.clear();
            for (CompanionInstance other : companions) {
                if (other == companion) {
                    continue;
                }
                Vec3 position = other.position();
                if (position != null) {
                    neighbourPositions.add(position);
                }
            }
            companion.tick(client, config, neighbourPositions);
        }
    }

    private boolean add(CompanionInstance companion, Minecraft client) {
        if (isFull()) {
            return false;
        }
        companions.add(companion);
        companion.tick(client, config, List.of());
        return true;
    }
}

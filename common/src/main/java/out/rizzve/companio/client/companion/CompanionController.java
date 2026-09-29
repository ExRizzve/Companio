package out.rizzve.companio.client.companion;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import out.rizzve.companio.Companio;
import out.rizzve.companio.client.companion.model.CompanionInstance;
import out.rizzve.companio.client.config.CompanioConfig;
import out.rizzve.companio.client.config.CompanionSlot;
import out.rizzve.companio.client.config.ConfigManager;
import out.rizzve.companio.client.skin.MojangProfileService;
import out.rizzve.companio.client.skin.ProfileCompat;

import java.util.UUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CompanionController {
    public static final int MAX_COMPANIONS = 16;

    private final List<CompanionInstance> companions = new ArrayList<>();
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

    public void sync(CompanioConfig config, Minecraft client, MojangProfileService profileService) {
        sync(config, client, profileService, null);
    }

    public void sync(
            CompanioConfig config,
            Minecraft client,
            MojangProfileService profileService,
            ConfigManager configManager
    ) {
        updateConfig(config);
        List<CompanionSlot> wanted = config.companions();

        while (companions.size() > wanted.size()) {
            companions.remove(companions.size() - 1).discard();
        }
        while (companions.size() < wanted.size()) {
            companions.add(new CompanionInstance(null));
        }

        for (int index = 0; index < wanted.size(); index++) {
            CompanionSlot slot = wanted.get(index);
            CompanionInstance companion = companions.get(index);
            companion.setHat(slot.hat());
            companion.setName(slot.name());
            if (!slot.playerName().equalsIgnoreCase(companion.playerName())) {
                applyProfile(companion, slot, index, client, profileService, configManager);
            }
            companion.tick(client, config, List.of());
        }
    }

    private void applyProfile(
            CompanionInstance companion,
            CompanionSlot slot,
            int index,
            Minecraft client,
            MojangProfileService profileService,
            ConfigManager configManager
    ) {
        String playerName = slot.playerName();
        if (slot.hasCachedSkin()) {
            companion.setProfile(playerName, ProfileCompat.fromTextures(
                    UUID.fromString(slot.profileId()), playerName, slot.textures(), slot.texturesSignature()));
            return;
        }

        companion.setProfile(playerName, null);
        if (playerName.isEmpty()) {
            return;
        }
        profileService.find(playerName).whenComplete((profile, error) -> client.execute(() -> {
            if (error != null) {
                Companio.LOGGER.error("Could not load profile for {}", playerName, error);
                return;
            }
            if (!playerName.equalsIgnoreCase(companion.playerName())) {
                return;
            }
            companion.setProfile(playerName, profile);
            cacheSkin(index, playerName, profile, configManager);
        }));
    }

    private void cacheSkin(int index, String playerName, GameProfile profile, ConfigManager configManager) {
        if (configManager == null || index >= config.companions().size()) {
            return;
        }
        CompanionSlot stored = config.companions().get(index);
        if (!stored.playerName().equalsIgnoreCase(playerName)) {
            return;
        }
        CompanioConfig updated = config.withCompanion(index, stored.withCachedSkin(
                ProfileCompat.id(profile).toString(),
                ProfileCompat.textures(profile),
                ProfileCompat.texturesSignature(profile)));
        updateConfig(updated);
        configManager.save(updated);
    }

    public boolean setName(int number, String name) {
        if (number < 1 || number > companions.size()) {
            return false;
        }
        companions.get(number - 1).setName(name);
        return true;
    }

    public CompanionHat hat(int number) {
        if (number < 1 || number > companions.size()) {
            return CompanionHat.NONE;
        }
        return companions.get(number - 1).hat();
    }

    public boolean setHat(int number, CompanionHat hat) {
        if (number < 1 || number > companions.size()) {
            return false;
        }
        companions.get(number - 1).setHat(hat);
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
        for (CompanionInstance companion : companions) {
            List<Vec3> positions = companions.stream()
                    .filter(other -> other != companion)
                    .map(CompanionInstance::position)
                    .filter(Objects::nonNull)
                    .toList();
            companion.tick(client, config, positions);
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

package out.rizzve.companio.client.skin;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.UUID;

public final class ProfileCompat {
    private ProfileCompat() {
    }

    public static String name(GameProfile profile) {
        return profile.getName();
    }

    public static UUID id(GameProfile profile) {
        return profile.getId();
    }

    public static GameProfile create(UUID id, String name, Multimap<String, Property> properties) {
        GameProfile profile = new GameProfile(id, name);
        profile.getProperties().putAll(properties);
        return profile;
    }

    public static ResolvableProfile resolvable(GameProfile profile) {
        return new ResolvableProfile(profile);
    }
}
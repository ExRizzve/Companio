package out.rizzve.companio.client.skin;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.UUID;

public final class ProfileCompat {
    private ProfileCompat() {
    }

    public static String name(GameProfile profile) {
        return profile.name();
    }

    public static UUID id(GameProfile profile) {
        return profile.id();
    }

    public static GameProfile create(UUID id, String name, Multimap<String, Property> properties) {
        return new GameProfile(id, name, new PropertyMap(properties));
    }

    public static ResolvableProfile resolvable(GameProfile profile) {
        return ResolvableProfile.createResolved(profile);
    }
}
package out.rizzve.companio.client.skin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MojangProfileService {
    private static final URI PROFILE_API = URI.create("https://api.mojang.com/users/profiles/minecraft/");
    private static final URI SESSION_API = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public CompletableFuture<GameProfile> find(String playerName) {
        if (playerName == null || !playerName.matches("[A-Za-z0-9_]{1,16}")) {
            return CompletableFuture.failedFuture(new ProfileException("companio.error.invalid_name"));
        }

        return get(PROFILE_API.resolve(playerName))
                .thenApply(this::readBasicProfile)
                .thenCompose(profile -> get(SESSION_API.resolve(profile.id().toString().replace("-", "") + "?unsigned=false")))
                .thenApply(this::readProfileWithTextures);
    }

    private CompletableFuture<String> get(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    }
                    if (response.statusCode() == 204 || response.statusCode() == 404) {
                        throw new ProfileException("companio.error.player_not_found");
                    }
                    throw new ProfileException("companio.error.service_status", response.statusCode());
                });
    }

    private GameProfile readBasicProfile(String body) {
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            return new GameProfile(parseUuid(json.get("id").getAsString()), json.get("name").getAsString());
        } catch (RuntimeException exception) {
            throw new ProfileException("companio.error.invalid_profile_response", exception);
        }
    }

    private GameProfile readProfileWithTextures(String body) {
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            JsonArray properties = json.getAsJsonArray("properties");
            ImmutableMultimap.Builder<String, Property> propertiesBuilder = ImmutableMultimap.builder();

            for (var element : properties) {
                JsonObject property = element.getAsJsonObject();
                String name = property.get("name").getAsString();
                String value = property.get("value").getAsString();
                String signature = property.has("signature") ? property.get("signature").getAsString() : null;
                propertiesBuilder.put(name, signature == null
                        ? new Property(name, value)
                        : new Property(name, value, signature));
            }
            return new GameProfile(
                    parseUuid(json.get("id").getAsString()),
                    json.get("name").getAsString(),
                    new PropertyMap(propertiesBuilder.build())
            );
        } catch (RuntimeException exception) {
            throw new ProfileException("companio.error.invalid_skin_response", exception);
        }
    }

    private static UUID parseUuid(String value) {
        if (value.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID");
        }
        return UUID.fromString(value.replaceFirst(
                "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
                "$1-$2-$3-$4-$5"
        ));
    }

    public static final class ProfileException extends RuntimeException {
        private final String translationKey;
        private final Object[] arguments;

        public ProfileException(String translationKey, Object... arguments) {
            super(translationKey);
            this.translationKey = translationKey;
            this.arguments = arguments;
        }

        public ProfileException(String translationKey, Throwable cause, Object... arguments) {
            super(translationKey, cause);
            this.translationKey = translationKey;
            this.arguments = arguments;
        }

        public Component toComponent() {
            return Component.translatable(translationKey, arguments);
        }
    }
}

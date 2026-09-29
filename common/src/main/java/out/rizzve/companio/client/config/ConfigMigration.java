package out.rizzve.companio.client.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ConfigMigration {
    private ConfigMigration() {
    }

    public static JsonElement apply(JsonElement root) {
        if (root == null || !root.isJsonObject()) {
            return root;
        }
        JsonObject object = root.getAsJsonObject();
        JsonElement companions = object.get("companions");
        if (companions == null || !companions.isJsonArray()) {
            return object;
        }

        JsonArray kept = new JsonArray();
        for (JsonElement element : companions.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject slot = element.getAsJsonObject();
            JsonElement enabled = slot.get("enabled");
            if (enabled != null && enabled.isJsonPrimitive() && !enabled.getAsBoolean()) {
                continue;
            }
            slot.remove("enabled");
            kept.add(slot);
        }
        object.add("companions", kept);
        return object;
    }
}

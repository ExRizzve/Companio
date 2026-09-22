package out.rizzve.companio.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import out.rizzve.companio.Companio;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path = FabricLoader.getInstance().getConfigDir().resolve("companio.json");

    public CompanioConfig load() {
        if (!Files.exists(path)) {
            save(CompanioConfig.DEFAULT);
            return CompanioConfig.DEFAULT;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            CompanioConfig loaded = GSON.fromJson(reader, CompanioConfig.class);
            if (loaded == null) {
                throw new IOException("configuration is empty");
            }

            CompanioConfig validated = loaded.validated();
            if (!validated.equals(loaded)) {
                Companio.LOGGER.warn("Invalid values in {}; defaults or safe limits were applied", path);
                save(validated);
            }
            return validated;
        } catch (Exception exception) {
            Companio.LOGGER.error("Could not read {}; default configuration will be used", path, exception);
            save(CompanioConfig.DEFAULT);
            return CompanioConfig.DEFAULT;
        }
    }

    public void save(CompanioConfig config) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config.validated(), writer);
            }
        } catch (IOException exception) {
            Companio.LOGGER.error("Could not save {}", path, exception);
        }
    }
}

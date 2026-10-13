package dev.wexyl.utils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.wexyl.utils.feature.Feature;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Zapisuje włączone/wyłączone funkcje w config/wexylutils.json. */
public final class ModConfig {

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("wexylutils.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Boolean>>() { }.getType();
    private static final Map<String, Boolean> VALUES = new HashMap<>();

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader reader = Files.newBufferedReader(FILE)) {
            Map<String, Boolean> loaded = GSON.fromJson(reader, MAP_TYPE);
            if (loaded != null) VALUES.putAll(loaded);
        } catch (IOException | RuntimeException e) {
            System.err.println("[WexylUtils] Nie udało się wczytać configu: " + e);
        }
    }

    public static boolean isEnabled(Feature feature) {
        return VALUES.getOrDefault(feature.id(), feature.defaultEnabled());
    }

    public static void set(Feature feature, boolean enabled) {
        VALUES.put(feature.id(), enabled);
        save();
    }

    private static void save() {
        try (Writer writer = Files.newBufferedWriter(FILE)) {
            GSON.toJson(VALUES, MAP_TYPE, writer);
        } catch (IOException e) {
            System.err.println("[WexylUtils] Nie udało się zapisać configu: " + e);
        }
    }

    private ModConfig() {
    }
}

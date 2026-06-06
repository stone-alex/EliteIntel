package elite.intel.ai.brain.actions.macro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import elite.intel.util.AppPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads and persists user macros from {@code macros.json} in the app data directory.
 * <p>
 * Callers are responsible for threading - {@link #save} should be called on a background thread.
 */
public final class MacroRepository {

    private static final Logger log = LogManager.getLogger(MacroRepository.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<MacroDefinition>>() {}.getType();

    /**
     * Loads all macros from {@code macros.json}. Returns an empty list if the file does not
     * exist or is empty. Invalid individual macros are skipped with an error log; the remaining
     * valid macros are still returned.
     */
    public List<MacroDefinition> load() {
        try {
            return load(AppPaths.getMacrosFilePath());
        } catch (Exception e) {
            log.error("Failed to resolve macros file path - no user macros will be available", e);
            return Collections.emptyList();
        }
    }

    /**
     * Package-private test seam - loads macros from an explicit {@link Path} without consulting
     * {@link AppPaths}. Production code always calls {@link #load()}.
     */
    List<MacroDefinition> load(Path path) {
        try {
            if (!Files.exists(path)) {
                log.info("macros.json not found at {} - no user macros loaded", path);
                return Collections.emptyList();
            }
            String json = Files.readString(path, StandardCharsets.UTF_8);
            if (json.isBlank()) {
                log.info("macros.json is empty - no user macros loaded");
                return Collections.emptyList();
            }
            List<MacroDefinition> raw = GSON.fromJson(json, LIST_TYPE);
            if (raw == null) {
                log.warn("macros.json parsed to null - treating as empty");
                return Collections.emptyList();
            }
            List<MacroDefinition> valid = new ArrayList<>();
            for (MacroDefinition def : raw) {
                try {
                    def.validate();
                    valid.add(def);
                    log.info("Loaded macro: '{}' (actionKey={} id={})",
                            def.getName(), def.getActionKey(), def.getId());
                } catch (IllegalArgumentException e) {
                    log.error("Skipping invalid macro entry: {}", e.getMessage());
                }
            }
            return Collections.unmodifiableList(valid);
        } catch (Exception e) {
            log.error("Failed to load macros.json - no user macros will be available", e);
            return Collections.emptyList();
        }
    }

    /**
     * Writes the macro list to {@code macros.json}, overwriting any existing content.
     * Caller must invoke this on a background thread.
     */
    public void save(List<MacroDefinition> macros) {
        trySave(macros);
    }

    /**
     * Writes macros and reports whether the runtime file was updated successfully.
     */
    public boolean trySave(List<MacroDefinition> macros) {
        try {
            return save(macros, AppPaths.getMacrosFilePath());
        } catch (Exception e) {
            log.error("Failed to resolve macros file path for save", e);
            return false;
        }
    }

    /**
     * Package-private test seam - saves macros to an explicit {@link Path}.
     * Production code always calls {@link #save(List)}.
     */
    boolean save(List<MacroDefinition> macros, Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            String json = GSON.toJson(macros);
            Files.writeString(path, json, StandardCharsets.UTF_8);
            log.info("Saved {} macro(s) to {}", macros.size(), path);
            return true;
        } catch (Exception e) {
            log.error("Failed to save macros.json", e);
            return false;
        }
    }
}

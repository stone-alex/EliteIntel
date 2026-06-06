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

    /** Number of macros skipped during the most recent {@link #load()} call due to validation failures. */
    private int lastSkippedCount = 0;
    /** Human-readable labels for macros skipped during the most recent {@link #load()} call. */
    private List<String> lastSkippedLabels = List.of();

    int getLastSkippedCount() { return lastSkippedCount; }
    List<String> getLastSkippedLabels() { return lastSkippedLabels; }

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
            int rawCount = raw.size();
            List<MacroDefinition> valid = new ArrayList<>();
            List<String> skipped = new ArrayList<>();
            for (MacroDefinition def : raw) {
                String label = macroLabel(def);
                try {
                    def.validate();
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping macro {}: {}", label, e.getMessage());
                    skipped.add(label);
                    continue;
                }
                List<String> formatErrors = MacroValidator.validateFormat(def);
                if (!formatErrors.isEmpty()) {
                    log.warn("Skipping macro {}: {}", label, String.join("; ", formatErrors));
                    skipped.add(label);
                    continue;
                }
                String ak = def.getActionKey();
                boolean duplicate = valid.stream().anyMatch(m -> m.getActionKey().equalsIgnoreCase(ak));
                if (duplicate) {
                    log.warn("Skipping macro {}: actionKey '{}' is a duplicate of an already-loaded macro", label, ak);
                    skipped.add(label);
                    continue;
                }
                valid.add(def);
                log.info("Loaded macro: '{}' (actionKey={} id={})", def.getName(), ak, def.getId());
            }
            lastSkippedCount = skipped.size();
            lastSkippedLabels = Collections.unmodifiableList(skipped);
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
    /** Returns the best human-readable identifier for a macro: name if present, else id + actionKey. */
    private static String macroLabel(MacroDefinition def) {
        String name = def.getName();
        if (name != null && !name.isBlank()) return name;
        String id = def.getId();
        String ak = def.getActionKey();
        if (ak != null && !ak.isBlank()) return (id != null ? id + " / " : "") + ak;
        return id != null ? id : "(unnamed)";
    }

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

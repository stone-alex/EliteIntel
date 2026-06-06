package elite.intel.ai.brain.actions.macro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import elite.intel.util.AppPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads and persists user macros from {@code macros.json} in the app data directory.
 * <p>
 * Saves are written to a {@code .tmp} file and atomically renamed to {@code macros.json},
 * reducing the risk of corruption on crash. A {@code macros.json.bak} backup of the previous
 * file is kept alongside. If the main file is unreadable or corrupt, the backup is automatically
 * tried and a diagnostic warning is logged.
 * <p>
 * Callers are responsible for threading - {@link #save} must be called on a background thread.
 */
public final class MacroRepository {

    private static final Logger log = LogManager.getLogger(MacroRepository.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<MacroDefinition>>() {}.getType();

    /** Number of macros skipped during the most recent {@link #load()} call due to validation failures. */
    private int lastSkippedCount = 0;
    /** Human-readable labels for macros skipped during the most recent {@link #load()} call. */
    private List<String> lastSkippedLabels = List.of();
    /** True if the most recent {@link #load()} call restored macros from the backup file. */
    private boolean restoredFromBackup = false;

    int getLastSkippedCount() { return lastSkippedCount; }
    List<String> getLastSkippedLabels() { return lastSkippedLabels; }
    boolean wasRestoredFromBackup() { return restoredFromBackup; }

    /**
     * Loads all macros from {@code macros.json}. Returns an empty list if the file does not
     * exist or is empty. If the main file is corrupt, automatically attempts to restore from
     * {@code macros.json.bak} with a logged warning. Invalid individual macros are skipped with
     * an error log; the remaining valid macros are still returned.
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
        resetLoadDiagnostics();
        Path backup = path.resolveSibling(path.getFileName() + ".bak");

        if (!Files.exists(path)) {
            log.info("macros.json not found at {} - no user macros loaded", path);
            return Collections.emptyList();
        }

        try {
            return parseAndFilter(path);
        } catch (Exception e) {
            log.warn("macros.json at {} could not be read ({}), attempting restore from backup",
                    path, e.getMessage());
        }

        if (!Files.exists(backup)) {
            log.error("macros.json is corrupt and no backup exists - no user macros will be available");
            return Collections.emptyList();
        }

        log.warn("Restoring user macros from backup: {}", backup);
        try {
            List<MacroDefinition> restored = parseAndFilter(backup);
            log.warn("Macro restore from backup succeeded: {} macro(s) loaded. Inspect macros.json for corruption.",
                    restored.size());
            restoredFromBackup = true;
            return restored;
        } catch (Exception e) {
            log.error("macros.json.bak is also invalid ({}) - no user macros will be available", e.getMessage());
            return Collections.emptyList();
        }
    }

    private void resetLoadDiagnostics() {
        lastSkippedCount = 0;
        lastSkippedLabels = List.of();
        restoredFromBackup = false;
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
     * <p>
     * The existing file is backed up to {@code <name>.bak} before the new data is written.
     * Data is first serialized and validated by round-trip, then written to {@code <name>.tmp},
     * and finally atomically renamed to replace the target file.
     */
    boolean save(List<MacroDefinition> macros, Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            String json = GSON.toJson(macros);

            // Guard: validate generated JSON parses back before touching any files.
            List<MacroDefinition> parsed = GSON.fromJson(json, LIST_TYPE);
            if (parsed == null) {
                log.error("Generated macro JSON failed round-trip validation - macros.json not updated");
                return false;
            }

            // Back up the current file so restore is possible if the new write is somehow lost.
            if (Files.exists(path)) {
                Path backup = path.resolveSibling(path.getFileName() + ".bak");
                try {
                    Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
                    log.debug("Backed up {} to {}", path.getFileName(), backup.getFileName());
                } catch (IOException e) {
                    log.warn("Could not create backup before saving macros - proceeding anyway: {}", e.getMessage());
                }
            }

            // Write to a temp file then atomically rename to minimize the corruption window.
            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(tmp, json, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                log.debug("Atomic move not supported on this filesystem - falling back to non-atomic replace");
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Saved {} macro(s) to {}", macros.size(), path);
            return true;
        } catch (Exception e) {
            log.error("Failed to save macros.json", e);
            return false;
        }
    }

    /**
     * Reads and parses macros from {@code path}, filtering out individually invalid entries.
     * Updates {@link #lastSkippedCount} and {@link #lastSkippedLabels} as a side-effect.
     *
     * @throws IOException              if the file cannot be read or its JSON top-level is not an array
     * @throws com.google.gson.JsonSyntaxException if the JSON is malformed
     */
    private List<MacroDefinition> parseAndFilter(Path path) throws IOException {
        String json = Files.readString(path, StandardCharsets.UTF_8);
        if (json.isBlank()) {
            log.info("Macro file at {} is empty - no user macros loaded", path);
            lastSkippedCount = 0;
            lastSkippedLabels = List.of();
            return Collections.emptyList();
        }
        List<MacroDefinition> raw = GSON.fromJson(json, LIST_TYPE);
        if (raw == null) {
            throw new IOException("JSON top-level is not an array in " + path.getFileName());
        }
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
    }

    /** Returns the best human-readable identifier for a macro: name if present, else id + actionKey. */
    private static String macroLabel(MacroDefinition def) {
        String name = def.getName();
        if (name != null && !name.isBlank()) return name;
        String id = def.getId();
        String ak = def.getActionKey();
        if (ak != null && !ak.isBlank()) return (id != null ? id + " / " : "") + ak;
        return id != null ? id : "(unnamed)";
    }
}

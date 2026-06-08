package elite.intel.ai.hands;

import elite.intel.util.AppPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Manages per-preset working copies of Elite Dangerous {@code .binds} files.
 * <p>
 * Working copies are stored in {@code elite-intel/bindings/} so the editor never
 * touches the game directory until the user explicitly applies via
 * {@link BindingsApplyService}. Each preset gets its own file keyed by the
 * original {@code .binds} filename.
 * <p>
 * The initial import is a byte-for-byte copy of the game file, preserving the
 * UTF-8 BOM if present. Subsequent edits are handled by {@link BindingsWriter}
 * which writes directly to the working copy path.
 */
public class BindingsWorkingCopyRepository {

    private static final Logger log = LogManager.getLogger(BindingsWorkingCopyRepository.class);

    /**
     * Returns the working copy path for the given preset file name.
     * The file may not exist yet.
     *
     * @throws IllegalStateException if the working directory cannot be resolved
     */
    public Path getWorkingCopyPath(String presetFileName) {
        try {
            return AppPaths.getBindingsWorkingDir().resolve(presetFileName);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot resolve bindings working directory", e);
        }
    }

    /**
     * Ensures a working copy exists for the given preset, importing from the game
     * file on first use. Returns the working copy {@link Path} for the caller to
     * use for parsing and editing.
     *
     * @param presetFileName filename of the active {@code .binds} file (e.g. {@code Custom.3.0.binds})
     * @param gameFile       path to the active game binds file; used only for the initial import
     */
    public Path loadOrImportFromGame(String presetFileName, Path gameFile) throws IOException {
        Path workingCopy = getWorkingCopyPath(presetFileName);
        if (Files.exists(workingCopy)) {
            log.debug("Using existing working copy for '{}' at {}", presetFileName, workingCopy);
            return workingCopy;
        }
        log.info("No working copy for '{}', importing from game file {}", presetFileName, gameFile);
        Files.createDirectories(workingCopy.getParent());
        // Byte-perfect copy preserves BOM and any encoding details of the original.
        Files.copy(gameFile, workingCopy);
        log.info("Working copy created at {}", workingCopy);
        return workingCopy;
    }

    /**
     * Atomically writes {@code xmlContent} as the working copy for the given preset.
     * A {@code .bak} sibling is created before overwriting to guard against corruption.
     * <p>
     * Note: {@link BindingsWriter} writes to the working copy directly via its own
     * atomic write; this method is used for bulk saves (e.g. reimport on revert).
     */
    public void save(String presetFileName, String xmlContent) throws IOException {
        Path workingCopy = getWorkingCopyPath(presetFileName);
        Files.createDirectories(workingCopy.getParent());

        if (Files.exists(workingCopy)) {
            Path bak = workingCopy.resolveSibling(workingCopy.getFileName() + ".bak");
            try {
                Files.copy(workingCopy, bak, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.warn("Could not create .bak for '{}' — proceeding: {}", workingCopy.getFileName(), e.getMessage());
            }
        }

        Path tmp = workingCopy.resolveSibling(workingCopy.getFileName() + ".tmp");
        Files.writeString(tmp, xmlContent, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            Files.move(tmp, workingCopy, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            log.debug("Atomic move not supported for working copy — falling back");
            Files.move(tmp, workingCopy, StandardCopyOption.REPLACE_EXISTING);
        }
        log.debug("Saved working copy for '{}' to {}", presetFileName, workingCopy);
    }

    /** Returns {@code true} if a working copy exists for the given preset. */
    public boolean exists(String presetFileName) {
        try {
            return Files.exists(getWorkingCopyPath(presetFileName));
        } catch (Exception e) {
            return false;
        }
    }

    /** Deletes the working copy and its {@code .bak} sibling for the given preset. */
    public void delete(String presetFileName) {
        try {
            Path workingCopy = getWorkingCopyPath(presetFileName);
            Files.deleteIfExists(workingCopy);
            Files.deleteIfExists(workingCopy.resolveSibling(workingCopy.getFileName() + ".bak"));
            log.info("Deleted working copy for '{}'", presetFileName);
        } catch (IOException e) {
            log.warn("Could not fully delete working copy for '{}': {}", presetFileName, e.getMessage());
        }
    }

    /**
     * Returns {@code true} if the working copy is byte-for-byte identical to the
     * game file. Returns {@code false} on any I/O error or if either file is absent.
     */
    public boolean isSyncedWithGame(String presetFileName, Path gameFile) {
        try {
            Path workingCopy = getWorkingCopyPath(presetFileName);
            if (!Files.exists(workingCopy) || !Files.exists(gameFile)) {
                return false;
            }
            return Files.mismatch(workingCopy, gameFile) == -1L;
        } catch (Exception e) {
            return false;
        }
    }
}

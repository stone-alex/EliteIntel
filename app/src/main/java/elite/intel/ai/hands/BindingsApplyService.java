package elite.intel.ai.hands;

import elite.intel.util.AppPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.*;
import java.util.UUID;

/**
 * Applies a working copy bindings file to the Elite Dangerous game directory.
 * <p>
 * The apply sequence is: read working copy → round-trip XML validation →
 * backup game file to {@code elite-intel/bindings/backups/} → atomic write to
 * game directory. The game directory is never touched until the user explicitly
 * requests apply.
 */
public class BindingsApplyService {

    private static final Logger log = LogManager.getLogger(BindingsApplyService.class);

    private final BindingsWorkingCopyRepository workingCopyRepo;
    private final BindingsBackupService backupService;

    public BindingsApplyService() {
        this(new BindingsWorkingCopyRepository(), new BindingsBackupService());
    }

    BindingsApplyService(BindingsWorkingCopyRepository workingCopyRepo, BindingsBackupService backupService) {
        this.workingCopyRepo = workingCopyRepo;
        this.backupService = backupService;
    }

    /**
     * Validates the working copy, backs up the current game file to
     * {@code elite-intel/bindings/backups/}, then atomically writes the working copy
     * to the game directory.
     *
     * @param presetFileName  the preset file name (e.g. {@code Custom.3.0.binds})
     * @param gameBindsFile   the active {@code .binds} file in the game directory
     * @return the path of the created backup, or {@code null} if the game file did not exist
     * @throws BindingsApplyException if validation fails or the write cannot complete
     */
    public Path apply(String presetFileName, Path gameBindsFile) throws BindingsApplyException {
        Path workingCopy = workingCopyRepo.getWorkingCopyPath(presetFileName);
        if (!Files.exists(workingCopy)) {
            throw new BindingsApplyException("No working copy found for preset: " + presetFileName);
        }

        byte[] content;
        try {
            content = Files.readAllBytes(workingCopy);
        } catch (IOException e) {
            throw new BindingsApplyException("Cannot read working copy: " + e.getMessage(), e);
        }

        validateXml(content);

        Path backupPath = backupGameFile(gameBindsFile);

        return writeToGameDir(gameBindsFile, content, backupPath);
    }

    private void validateXml(byte[] content) throws BindingsApplyException {
        // Strip UTF-8 BOM before feeding to the XML parser, which may reject it.
        String xml;
        if (content.length >= 3
                && content[0] == (byte) 0xEF
                && content[1] == (byte) 0xBB
                && content[2] == (byte) 0xBF) {
            xml = new String(content, 3, content.length - 3, java.nio.charset.StandardCharsets.UTF_8);
        } else {
            xml = new String(content, java.nio.charset.StandardCharsets.UTF_8);
        }
        try {
            DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new BindingsApplyException("Bindings file failed XML validation: " + e.getMessage(), e);
        }
    }

    private Path backupGameFile(Path gameBindsFile) throws BindingsApplyException {
        if (!Files.exists(gameBindsFile)) {
            log.info("No existing game file to back up at {}", gameBindsFile);
            return null;
        }
        try {
            Path backupDir = AppPaths.getBindingsBackupDir();
            Path backupPath = backupService.createBackup(gameBindsFile, backupDir);
            log.info("Backed up game bindings to {}", backupPath);
            return backupPath;
        } catch (IOException e) {
            throw new BindingsApplyException("Could not create backup of game bindings: " + e.getMessage(), e);
        }
    }

    private Path writeToGameDir(Path gameBindsFile, byte[] content, Path backupPath) throws BindingsApplyException {
        Path parent = gameBindsFile.getParent();
        Path tmp = parent.resolve("." + gameBindsFile.getFileName()
                + ".elite-intel-" + UUID.randomUUID() + ".tmp");
        try {
            Files.createDirectories(parent);
            Files.write(tmp, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            try {
                Files.move(tmp, gameBindsFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                log.debug("Atomic move not supported for apply — falling back");
                Files.move(tmp, gameBindsFile, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Applied bindings to {}", gameBindsFile);
            return backupPath;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignored) {
            }
            throw new BindingsApplyException("Could not write bindings to game directory: " + e.getMessage(), e);
        }
    }
}

package elite.intel.ai.hands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Creates non-profile backups for Elite Dangerous {@code .binds} files.
 * <p>
 * Backup names intentionally do not end with {@code .binds}; otherwise Elite
 * Dangerous and {@link BindingsLoader} could treat backups as loadable profiles.
 */
public class BindingsBackupService {
    private static final DateTimeFormatter BACKUP_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Clock clock;

    public BindingsBackupService() {
        this(Clock.systemDefaultZone());
    }

    BindingsBackupService(Clock clock) {
        this.clock = clock;
    }

    /**
     * Copies the original file next to itself using a timestamped, collision-safe
     * {@code .bak} name.
     */
    public Path createBackup(Path bindsFile) throws IOException {
        Path directory = bindsFile.getParent();
        String timestamp = ZonedDateTime.now(clock).format(BACKUP_TIMESTAMP);
        String baseName = bindsFile.getFileName() + ".elite-intel-backup-" + timestamp;

        for (int attempt = 0; attempt < 100; attempt++) {
            String suffix = attempt == 0 ? "" : "-" + attempt;
            Path backup = directory.resolve(baseName + suffix + ".bak");
            if (backup.getFileName().toString().endsWith(".binds")) {
                throw new IOException("Backup filename must not end with .binds: " + backup);
            }
            if (!Files.exists(backup)) {
                return Files.copy(bindsFile, backup, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }

        throw new IOException("Could not create a unique backup filename for " + bindsFile);
    }
}

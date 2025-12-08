package elite.intel.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AppPaths {

    private static Path APP_DIR;

    private AppPaths() {
    }

    public static Path getAppDirectory() {
        return APP_DIR;
    }

    public static Path getDatabasePath() throws IOException {
        Path base;
        if (OsDetector.getOs() == OsDetector.OS.LINUX || OsDetector.getOs() == OsDetector.OS.MAC) {
            String dataHome = System.getenv("XDG_DATA_HOME");
            base = dataHome != null && !dataHome.isEmpty()
                    ? Path.of(dataHome)
                    : Path.of(System.getProperty("user.home"), ".local/share");
        } else if (OsDetector.getOs() == OsDetector.OS.WINDOWS) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData == null || localAppData.isEmpty()) {
                throw new IllegalStateException("LOCALAPPDATA not set");
            }
            base = Path.of(localAppData);
        } else {
            throw new IllegalStateException("Unsupported OS");
        }

        Path dbDir = base.resolve("elite-intel/db");
        Files.createDirectories(dbDir);  // Ensure it exists
        return dbDir.resolve("database.db");
    }

    public static Path getConfigDirectory() {
        return APP_DIR.resolve("config");
    }

    public static Path getLogsDirectory() {
        return APP_DIR.resolve("logs");
    }

    public static String getSecretKeyFile() {
        if (OsDetector.getOs() == OsDetector.OS.LINUX || OsDetector.getOs() == OsDetector.OS.MAC) {
            return System.getProperty("user.home")
                    + File.separator
                    + ".local"
                    + File.separator
                    + "share"
                    + File.separator
                    + "elite-intel"
                    + File.separator
                    + "secret.key";
        } else {
            return System.getenv("LOCALAPPDATA")
                    + File.separator
                    + "elite-intel"
                    + File.separator
                    + "secret.key";
        }
    }

    static {
        Path dir = null;

        // start optimistic
        try {
            // CASE 1: Running from a JAR → use the JAR's folder
            Path codeSource = Path.of(
                    AppPaths.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            // If it's a JAR file → use its parent directory
            if (codeSource.toString().endsWith(".jar")) {
                dir = codeSource.getParent();
            } else {
                // CASE 2: Running from IDE → walk up to find project root (build.gradle)
                Path current = codeSource;
                while (current != null) {
                    if (Files.exists(current.resolve("build.gradle")) ||
                            Files.exists(current.resolve("build.gradle.kts")) ||
                            Files.exists(current.resolve("settings.gradle"))) {
                        dir = current;
                        break;
                    }
                    current = current.getParent();
                }
            }
        } catch (Exception e) {
            // ignore — will fall back
        }

        // FINAL FALLBACK: current working directory (./)
        if (dir == null) {
            dir = Path.of(".").toAbsolutePath().normalize();
        }

        APP_DIR = dir;
    }
}
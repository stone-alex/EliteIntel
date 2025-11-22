package elite.intel.util;

import java.nio.file.Files;
import java.nio.file.Path;

public final class AppPaths {

    private static Path APP_DIR;

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
                dir = codeSource.getParent(); // e.g. /home/alex/releases/elite_intel.jar → /home/alex/releases
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

    private AppPaths() {}

    public static Path getAppDirectory() {
        return APP_DIR;
    }

    public static Path getDatabasePath() {
        return APP_DIR.resolve("db/database.db");
    }

    public static Path getConfigDirectory() {
        return APP_DIR.resolve("config");
    }

    public static Path getLogsDirectory() {
        return APP_DIR.resolve("logs");
    }
}
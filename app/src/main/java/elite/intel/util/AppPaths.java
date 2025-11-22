package elite.intel.util;

import java.nio.file.Files;
import java.nio.file.Path;


public final class AppPaths {

    private static Path APP_DIR;

    static {
        try {
            // 1. Try to find the real project root via Gradle-aware logic
            Path candidate = findProjectRoot();
            if (candidate != null && Files.exists(candidate.resolve("build.gradle"))) {
                APP_DIR = candidate;
            } else {
                // 2. Fallback: go up from the running code location
                Path codeSource = Path.of(
                        AppPaths.class.getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .toURI()
                );

                Path temp = codeSource;
                // Go up until we find a folder containing build.gradle or settings.gradle
                while (temp != null && temp.getParent() != null) {
                    if (Files.exists(temp.resolve("build.gradle")) ||
                            Files.exists(temp.resolve("settings.gradle"))) {
                        APP_DIR = temp;
                        break;
                    }
                    temp = temp.getParent();
                }
                // Final fallback
                if (temp == null) throw new Exception();
            }
        } catch (Exception e) {
            // 3. Nuclear fallback — should never happen
            APP_DIR = Path.of(".").toAbsolutePath().normalize();
        }
    }

    private AppPaths() {}

    /** Smart detection: walks up from current dir looking for Gradle project markers */
    private static Path findProjectRoot() {
        Path current = Path.of(".").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("build.gradle")) ||
                    Files.exists(current.resolve("settings.gradle")) ||
                    Files.exists(current.resolve("gradlew"))) {
                return current;
            }
            if (current.getParent() == null) break;
            current = current.getParent();
        }
        return null;
    }

    public static Path getAppDirectory() {
        return APP_DIR;
    }

    public static Path getDatabasePath() {
        return APP_DIR.resolve("db/database.db");
    }

    public static Path getConfigDirectory() {
        return APP_DIR.resolve("config");
    }
}
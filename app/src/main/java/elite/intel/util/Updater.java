package elite.intel.util;

import elite.intel.session.SystemSession;
import elite.intel.util.OsDetector.OS;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Updater {

    private static final Path JAR_DIR = getJarDirectory();

    private Updater() {
    }

    private static Path getJarDirectory() {
        try {
            String jarPath = Updater.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            return Path.of(jarPath).getParent();
        } catch (Exception e) {
            throw new RuntimeException("Cannot detect JAR directory", e);
        }
    }

    /**
     * Executes an update operation asynchronously by extracting the appropriate
     * platform-specific update script to a temporary directory, running it, and managing the
     * temporary files. The method handles platform-specific commands for Windows and Linux.
     *
     * The update operation invokes a script to potentially modify or update local files
     * based on the current application setup.
     *
     * @return a {@code CompletableFuture<Boolean>} that completes with {@code true} if the
     *         update process starts successfully, or {@code false} if an error occurs during
     *         the setup or execution of the process.
     */
    public static CompletableFuture<Boolean> performUpdateAsync() {
        return CompletableFuture.supplyAsync(() -> {

            Path tempDir = null;
            boolean success = false;
            try {
                tempDir = Files.createTempDirectory("elite-intel-update");
                Path script = extractPlatformScript(tempDir);

                List<String> command = OsDetector.getOs() == OS.WINDOWS
                        ? List.of("cmd.exe", "/c", script.toString(), JAR_DIR.toString())
                        : List.of("bash", script.toString(), JAR_DIR.toString());

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(tempDir.toFile());

                pb.inheritIO();

                Process process = pb.start();
                if (process.isAlive()) {
                    success = true;
                    return true;
                }
                return false;
            } catch (IOException e) {
                return false;
            } finally {
                if (tempDir != null && !success) {
                    cleanupTemp(tempDir);
                }
            }
        });
    }

    private static Path extractPlatformScript(Path tempDir) throws IOException {
        String scriptName = OsDetector.getOs() == OS.WINDOWS ? "update.bat" : "update.sh";
        try (InputStream is = Updater.class.getResourceAsStream("/scripts/" + scriptName)) {
            if (is == null) throw new IOException("Missing update script: " + scriptName);
            Path script = tempDir.resolve(scriptName);
            Files.copy(is, script, StandardCopyOption.REPLACE_EXISTING);
            if (!scriptName.endsWith(".bat")) {
                Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwxr-xr-x"));
            }
            return script;
        }
    }

    private static void cleanupTemp(Path dir) {
        try (var stream = Files.walk(dir)) {
            stream.sorted((a, b) -> -a.compareTo(b))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    /**
     * Checks asynchronously if a newer version of the software is available by comparing the local version
     * with the remote version fetched from a predefined resource URL.
     *
     * @return a {@code CompletableFuture<Boolean>} that completes with {@code true} if a newer version
     *         is available, or {@code false} if the local version is up-to-date or an error occurs during the check.
     */
    public static CompletableFuture<Boolean> isUpdateAvailableAsync() {
        return CompletableFuture.supplyAsync(() -> {
            SystemSession.getInstance().readVersionFromResources();
            String local = SystemSession.getInstance().readVersionFromResources();
            if (local == null || local.isBlank()) return false;

            Long localVersion = StringUtls.getNumericBuild(local);

            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://raw.githubusercontent.com/stone-alex/EliteIntel/master/app/src/main/resources/version.txt"))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    String remote = response.body().trim();
                    Long remoteVersion = StringUtls.getNumericBuild(remote);
                    return remoteVersion > localVersion;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}
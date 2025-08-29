package elite.companion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.net.URI;

public class GoogleApiKeyProvider {
    private static final Logger log = LoggerFactory.getLogger(GoogleApiKeyProvider.class);
    private static final GoogleApiKeyProvider INSTANCE = new GoogleApiKeyProvider();
    public static final String GOOGLE_API_KEY_FILENAME = "GoogleApiKeyFile.json";
    private final String APP_DIR;

    private GoogleApiKeyProvider() {
        // Initialize APP_DIR
        String appDir = ""; // Default to empty string for development
        try {
            URI jarUri = GoogleApiKeyProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarFile = new File(jarUri);
            if (jarFile.getPath().endsWith(".jar")) {
                String parentDir = jarFile.getParent();
                if (parentDir != null) {
                    appDir = parentDir + File.separator;
                    log.debug("Running from JAR, set APP_DIR to: {}", appDir);
                } else {
                    log.warn("JAR parent directory is null, using empty APP_DIR: {}", appDir);
                }
            } else {
                log.debug("Not running from JAR, using empty APP_DIR for classpath resources");
            }
        } catch (Exception e) {
            log.warn("Could not determine JAR location, using empty APP_DIR: {}. Error: {}", appDir, e.getMessage());
        }
        APP_DIR = appDir;
    }

    public static GoogleApiKeyProvider getInstance() {
        return INSTANCE;
    }

    public InputStream getGoogleApiKeyStream() throws IOException {
        File file = new File(APP_DIR + GOOGLE_API_KEY_FILENAME);
        if (file.exists()) {
            try {
                log.debug("Reading Google API key file from: {}", file.getAbsolutePath());
                return new FileInputStream(file);
            } catch (IOException e) {
                log.error("Error reading Google API key file {}: {}", file.getAbsolutePath(), e.getMessage());
                throw e;
            }
        } else {
            // Fallback to resources
            InputStream input = getClass().getClassLoader().getResourceAsStream(GOOGLE_API_KEY_FILENAME);
            if (input == null) {
                String errorMessage = GOOGLE_API_KEY_FILENAME + " is not found";
                System.err.println(errorMessage);
                log.error(errorMessage);
                throw new IOException(errorMessage);
            }
            log.debug("Reading Google API key file from resources: {}", GOOGLE_API_KEY_FILENAME);
            return input;
        }
    }
}
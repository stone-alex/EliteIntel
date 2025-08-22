package elite.companion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Globals {
    public static final String GOOGLE_API_KEY = "/elite-companion-google-api-key.json";
    public static final String XAI_API_KEY = "/elite-companion-xai-api-key.conf";

    public static String readConfigFile(String resourcePath, String key) {
        try (InputStream is = Globals.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Resource file not found: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line = reader.lines().collect(Collectors.joining("\n"));
                if (line == null || line.isEmpty() || !line.contains(key + "=")) {
                    throw new RuntimeException("Invalid config format or missing key: " + key);
                }
                return line.substring(line.indexOf(key + "=") + key.length() + 1).trim();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading resource file: " + resourcePath, e);
        }
    }

}

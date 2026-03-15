package elite.intel.util.json;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A utility class for handling JSON operations with safety and default values.
 * This class provides static methods to extract values from {@link JsonObject}
 * in a null-safe manner, returning default empty values when necessary.
 * <p>
 * The purpose of this class is to simplify JSON parsing and handling common edge cases,
 * such as missing keys, `null` values, and unexpected types.
 * <p>
 * This class is designed to be used statically and cannot be instantiated.
 */
public final class JsonUtils {
    private static final Logger log = LogManager.getLogger(JsonUtils.class);

    private JsonUtils() {
        // Prevent instantiation
    }

    public static String getAsStringOrEmpty(JsonObject obj, String key) {
        if (obj == null || key == null) return "";
        if (!obj.has(key)) return "";
        var el = obj.get(key);
        if (el == null || el.isJsonNull()) return "";
        if (el.isJsonPrimitive()) {
            try {
                return el.getAsString();
            } catch (UnsupportedOperationException ignored) {
                // fallthrough
            }
        }
        log.debug("Expected string for key '{}' but got {}", key, el);
        return "";
    }

    /**
     * Repairs common LLM JSON output failures before parsing.
     * Handles the case where a model outputs an unquoted string value for {@code response_text},
     * e.g. {@code {"type":"chat","response_text": Hello Commander}} instead of quoting the value.
     */
    public static String repairLlmJson(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"response_text\"\\s*:\\s*(?!\")(.+?)\\s*}\\s*$",
                java.util.regex.Pattern.DOTALL
        );
        java.util.regex.Matcher m = p.matcher(raw);
        if (m.find()) {
            String value = m.group(1)
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            return raw.substring(0, m.start()) + "\"response_text\": \"" + value + "\"}";
        }
        return raw;
    }

    public static JsonObject nullSaveJsonObject(JsonObject obj, String key, Logger log) {
        if (obj == null || key == null) return new JsonObject();
        if (!obj.has(key)) return new JsonObject();
        var el = obj.get(key);
        if (el == null || el.isJsonNull()) return new JsonObject();
        if (el.isJsonObject()) return el.getAsJsonObject();
        log.debug("Expected object for key '{}' but got {}", key, el);
        return new JsonObject();
    }

}
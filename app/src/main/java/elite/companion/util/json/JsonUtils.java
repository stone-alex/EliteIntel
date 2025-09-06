package elite.companion.util.json;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonUtils {
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

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

    public static JsonObject getAsObjectOrEmpty(JsonObject obj, String key) {
        if (obj == null || key == null) return new JsonObject();
        if (!obj.has(key)) return new JsonObject();
        var el = obj.get(key);
        if (el == null || el.isJsonNull()) return new JsonObject();
        if (el.isJsonObject()) return el.getAsJsonObject();
        log.debug("Expected object for key '{}' but got {}", key, el);
        return new JsonObject();
    }
}
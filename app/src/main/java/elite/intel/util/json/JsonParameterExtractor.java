package elite.intel.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A utility class for extracting a specific JSON parameter from a JSON object based on a placeholder key.
 * <p>
 * The class provides a static method to retrieve a JSON element from a {@link JsonObject}
 * by using a placeholder string as the key. The placeholder key may contain surrounding angle brackets
 * ("<" and ">") that will be stripped before performing the lookup.
 * <p>
 * This class is designed for static use and does not support instantiation.
 * <p>
 * Features:
 * - Handles null or empty inputs for the placeholder key and JSON object gracefully.
 * - Strips angle brackets ("<" and ">") from the placeholder string to form the actual key.
 * - Retrieves the mapped {@link JsonElement} corresponding to the key from the JSON object.
 * <p>
 * Note:
 * - Returns `null` if the key does not exist in the JSON object or if the inputs are invalid.
 */
public class JsonParameterExtractor {
    public static JsonElement extractParameter(String commandActionPlaceHolder, JsonObject params) {
        if (commandActionPlaceHolder == null || commandActionPlaceHolder.isEmpty()) return null;
        if (params == null || params.isEmpty()) return null;
        return params.get(commandActionPlaceHolder.replace("<", "").replace(">", ""));
    }

}

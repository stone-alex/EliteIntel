package elite.companion.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * A utility class for creating JSON responses from Java objects.
 * This class provides static methods to convert data into a JSON response structure
 * with a specified response text and an array of data.
 * <p>
 * The resulting JSON response includes the following structure:
 * - A "response_text" field for the provided message.
 * - A "data" field containing an array of serialized data.
 * <p>
 * The methods in this class operate on either strings directly or objects
 * of type {@code PrimitiveData}.
 * <p>
 * This class is designed to be used statically and cannot be instantiated.
 */
public class JsonDataUtils {
    public static JsonObject toJsonResponse(List<String> data, String responseText) {
        JsonObject response = new JsonObject();
        response.addProperty("response_text", responseText);
        JsonArray dataArray = new JsonArray();
        for (String item : data) {
            dataArray.add(item);
        }
        response.add("data", dataArray);
        return response;
    }

    public static JsonObject toJsonResponseFromPrimitive(List<PrimitiveData> data, String responseText) {
        JsonObject response = new JsonObject();
        response.addProperty("response_text", responseText);
        JsonArray dataArray = new JsonArray();
        for (PrimitiveData item : data) {
            dataArray.add(item.getValue());
        }
        response.add("data", dataArray);
        return response;
    }
}
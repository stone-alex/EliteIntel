package elite.companion.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

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
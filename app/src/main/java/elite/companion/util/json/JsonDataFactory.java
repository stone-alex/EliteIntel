package elite.companion.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Collection;
import java.util.List;

public class JsonDataFactory {
    private static final JsonDataFactory INSTANCE = new JsonDataFactory();

    private JsonDataFactory() {
    }

    public static JsonDataFactory getInstance() {
        return INSTANCE;
    }

    public String toJsonString(ToJsonConvertible data) {
        if (data == null) {
            return null;
        }
        try {
            JsonParser.parseString(data.toJson());
            return data.toJson();
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public String toJsonArrayString(Collection<? extends ToJsonConvertible> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        JsonArray jsonArray = new JsonArray();
        for (ToJsonConvertible value : values) {
            try {
                jsonArray.add(JsonParser.parseString(value.toJson()));
            } catch (JsonSyntaxException e) {
                // Skip invalid entries
                continue;
            }
        }
        return jsonArray.size() > 0 ? GsonFactory.getGson().toJson(jsonArray) : null;
    }

    public String toJsonArrayStringFromList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        JsonArray jsonArray = new JsonArray();
        for (String value : values) {
            jsonArray.add(value);
        }
        return GsonFactory.getGson().toJson(jsonArray);
    }

    public JsonObject toJsonObject(String data) {
        if (data == null) {
            return null;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", data);
        return jsonObject;
    }

    /**
     * Validates a JSON string.
     *
     * @param jsonStr JSON string to validate.
     * @return true if valid, false otherwise.
     */
    public boolean isValidJson(String jsonStr) {
        try {
            JsonParser.parseString(jsonStr);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}
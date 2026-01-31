package elite.intel.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GetNumberFromParam {

    public static Number extractRangeParameter(JsonObject params, int defaultValue) {
        JsonElement element = params.get("max_distance");
        if (element != null && element.isJsonPrimitive() && element.getAsString().matches("\\d+")) {
            return element.getAsNumber();
        }
        return defaultValue;
    }
}

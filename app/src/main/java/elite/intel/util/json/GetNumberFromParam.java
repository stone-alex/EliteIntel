package elite.intel.util.json;

import com.google.gson.JsonObject;

public class GetNumberFromParam {

    public static Number getNumberFromParam(JsonObject params, int defaultValue) {
        if (params.get("key") != null && params.get("key").isJsonPrimitive() && params.get("key").getAsString().matches("\\d+")) {
            return params.get("key").getAsNumber();
        }
        return defaultValue;
    }
}

package elite.companion.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonParameterExtractor {
    public static JsonElement extractParameter(String commandActionPlaceHolder, JsonObject params) {
        if (commandActionPlaceHolder == null || commandActionPlaceHolder.isEmpty()) return null;
        if (params == null || params.isEmpty()) return null;
        return params.get(commandActionPlaceHolder.replace("<", "").replace(">", ""));
    }

}

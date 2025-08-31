package elite.companion.comms.handlers.query;

import ch.qos.logback.core.util.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.companion.session.PlayerSession;

import java.util.List;

public class GenericResponse {
    private static GenericResponse instance;

    private GenericResponse() {
    }

    public static GenericResponse getInstance() {
        if (instance == null) {
            instance = new GenericResponse();
        }
        return instance;
    }

    public JsonObject genericResponse(String text) {
        PlayerSession session = PlayerSession.getInstance();
        String playerName = StringUtil.nullStringToEmpty(String.valueOf(session.get(PlayerSession.PLAYER_NAME)));

        JsonObject response = new JsonObject();
        response.addProperty("response_text", text + " " + ("null".equalsIgnoreCase(playerName) ? "Commander" : playerName));
        return response;
    }

    public JsonObject genericResponseWithData(String text, JsonElement data) {
        JsonObject response = genericResponse(text);
        response.add("data", data);
        return response;
    }

    public JsonObject genericResponseWithList(String text, List<String> data) {
        JsonObject response = genericResponse(text);
        JsonArray dataArray = new JsonArray();
        for (String item : data) {
            dataArray.add(item);
        }
        response.add("data", dataArray);
        return response;
    }
}
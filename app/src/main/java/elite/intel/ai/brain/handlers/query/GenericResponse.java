package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.AIConstants;
import elite.intel.session.PlayerSession;

import java.util.List;

/**
 * The GenericResponse class provides methods for generating standardized JSON responses
 * with optional data elements or lists. This class implements a singleton pattern
 * to ensure a single shared instance across usage.
 */
public class GenericResponse {
    private static GenericResponse instance;

    private GenericResponse() {
        // Prevent instantiation.
    }

    public static GenericResponse getInstance() {
        if (instance == null) {
            instance = new GenericResponse();
        }
        return instance;
    }

    public JsonObject genericResponse(String text) {
        PlayerSession session = PlayerSession.getInstance();
        String playerName = session.getPlayerName();

        JsonObject response = new JsonObject();
        response.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, text + " " + ("null".equalsIgnoreCase(playerName) ? "Commander" : playerName));
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
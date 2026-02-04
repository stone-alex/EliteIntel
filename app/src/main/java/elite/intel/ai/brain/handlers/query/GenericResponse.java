package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.AIConstants;
import elite.intel.session.PlayerSession;

public class GenericResponse {
    private static GenericResponse instance;
    private final PlayerSession session = PlayerSession.getInstance();

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
        String playerName = session.getPlayerName();
        JsonObject response = new JsonObject();
        response.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, text + " " + playerName);
        return response;
    }
}
package elite.companion.ai.brain.handlers.query;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.companion.ai.search.api.EdsmApiClient;
import elite.companion.ai.search.api.SpanshApiClient;
import elite.companion.session.PlayerSession;

/**
 * MaterialTraderQueryHandler is responsible for processing queries to locate the nearest material
 * trader based on the user's current location in the game universe. It integrates with external
 * APIs such as Spansh and EDSM to perform the search and provide relevant responses.
 */
public class MaterialTraderQueryHandler implements QueryHandler {
    /**
     * Handles the query for finding the nearest material trader based on the current system.
     * Uses Spansh API as the primary source and falls back to EDSM API if no results are found.
     *
     * @param action            The requested action to be performed.
     * @param params            Additional parameters provided for the query handling process.
     * @param originalUserInput The original input from the user triggering this handler.
     * @return A JsonObject containing the result of the query, including information about the nearest material trader
     * and any additional metadata for further actions.
     * @throws Exception If an error occurs during the process of querying or constructing a response.
     */
    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        JsonObject response = new JsonObject();
        PlayerSession playerSession = PlayerSession.getInstance();
        String currentSystem = String.valueOf(playerSession.get(PlayerSession.CURRENT_SYSTEM));

        // Try Spansh first
        JsonArray stations = SpanshApiClient.searchStations(currentSystem, "materialtrader", null, null);
        if (!stations.isEmpty()) {
            JsonObject nearest = stations.get(0).getAsJsonObject();
            String stationName = nearest.get("name").getAsString();
            String systemName = nearest.get("system").getAsString();
            float distance = nearest.get("distance").getAsFloat();
            playerSession.put(systemName, PlayerSession.CURRENT_STATUS); // Store target
            response.addProperty("response_text", "Nearest material trader is at " + stationName + " in " + systemName + ", " + distance + " light years away. Say 'plot route' to navigate there.");
        } else {
            // Fallback to EDSM
            stations = EdsmApiClient.searchStations(currentSystem, "materialtrader");
            if (!stations.isEmpty()) {
                JsonObject nearest = stations.get(0).getAsJsonObject();
                String stationName = nearest.get("stationName").getAsString();
                String systemName = nearest.get("systemName").getAsString();
                playerSession.put(systemName, PlayerSession.CURRENT_STATUS); // Store target
                response.addProperty("response_text", "Nearest material trader via EDSM is at " + stationName + " in " + systemName + ". Say 'plot route' to navigate there.");
            } else {
                response.addProperty("response_text", "No material traders found near " + currentSystem + ".");
            }
        }

        response.addProperty("action", "find_material_trader");
        response.add("params", new JsonObject());
        response.addProperty("expect_followup", false);
        return response;
    }
}
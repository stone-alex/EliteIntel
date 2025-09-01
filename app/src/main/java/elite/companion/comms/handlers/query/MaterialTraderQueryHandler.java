package elite.companion.comms.handlers.query;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.companion.search.api.EdsmApiClient;
import elite.companion.search.api.SpanshApiClient;
import elite.companion.session.SystemSession;

public class MaterialTraderQueryHandler implements QueryHandler {
    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        JsonObject response = new JsonObject();
        SystemSession systemSession = SystemSession.getInstance();
        String currentSystem = String.valueOf(systemSession.get(SystemSession.CURRENT_SYSTEM));

        // Try Spansh first
        JsonArray stations = SpanshApiClient.searchStations(currentSystem, "materialtrader", null, null);
        if (!stations.isEmpty()) {
            JsonObject nearest = stations.get(0).getAsJsonObject();
            String stationName = nearest.get("name").getAsString();
            String systemName = nearest.get("system").getAsString();
            float distance = nearest.get("distance").getAsFloat();
            systemSession.put(systemName, SystemSession.CURRENT_STATUS); // Store target
            response.addProperty("response_text", "Nearest material trader is at " + stationName + " in " + systemName + ", " + distance + " light years away. Say 'plot route' to navigate there.");
        } else {
            // Fallback to EDSM
            stations = EdsmApiClient.searchStations(currentSystem, "materialtrader");
            if (!stations.isEmpty()) {
                JsonObject nearest = stations.get(0).getAsJsonObject();
                String stationName = nearest.get("stationName").getAsString();
                String systemName = nearest.get("systemName").getAsString();
                systemSession.put(systemName, SystemSession.CURRENT_STATUS); // Store target
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
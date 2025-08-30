package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.comms.ai.GrokAnalysisEndpoint;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.GsonFactory;

import java.util.Collection;

import static elite.companion.session.SystemSession.CURRENT_SYSTEM;

public class AnalyzeDataHandler implements QueryHandler {

    @Override
    public String handle(String action, JsonObject params, String originalUserInput) {
        QueryActions query = findQuery(action);
        String dataJson = fetchDataForAction(query);
        if (dataJson == null || dataJson.isEmpty()) {
            JsonObject response = new JsonObject();
            response.addProperty("response_text", "No data available, My Lord.");
            return GsonFactory.getGson().toJson(response);
        }

        if (query == QueryActions.QUERY_TELL_ME_YOUR_NAME) {
            return dataJson; // Return directly for simple query
        }

        GrokAnalysisEndpoint grokAnalysisEndpoint = GrokAnalysisEndpoint.getInstance();
        String analysisJson = grokAnalysisEndpoint.analyzeData(originalUserInput, dataJson);
        return analysisJson;
    }

    private QueryActions findQuery(String action) {
        QueryActions[] actions = QueryActions.values();
        for (QueryActions qa : actions) {
            if (qa.getAction().equals(action)) {
                return qa;
            }
        }
        VoiceGenerator.getInstance().speak("Sorry, no query action found for: " + action);
        throw new IllegalArgumentException("No query action found for: " + action);
    }

    private String fetchDataForAction(QueryActions action) {
        SystemSession systemSession = SystemSession.getInstance();
        PlayerSession playerSession = PlayerSession.getInstance();
        systemSession.get(CURRENT_SYSTEM);

        switch (action) {
            case QUERY_SEARCH_SIGNAL_DATA:
                return GsonFactory.getGson().toJson(String.valueOf(systemSession.getSignals()));

            case QUERY_SHIP_LOADOUT:
                return String.valueOf(systemSession.get(SystemSession.SHIP_LOADOUT_JSON));

            case QUERY_ANALYZE_ROUTE:
                return getRoute();

            case QUERY_ANALYZE_ON_BOARD_CARGO:
                return GsonFactory.getGson().toJson(SystemSession.getInstance().get(SystemSession.SHIP_CARGO));

            case LOCAL_SYSTEM_INFO:
                return GsonFactory.getGson().toJson(systemSession.get(CURRENT_SYSTEM));

            case CHECK_LEGAL_STATUS:
                return GsonFactory.getGson().toJson(systemSession.get(SystemSession.CURRENT_STATUS));

            case QUERY_NEXT_STAR_SCOOPABLE:
                return GsonFactory.getGson().toJson(getRoute());

            case QUERY_CARRIER_STATS:
                return GsonFactory.getGson().toJson(playerSession.get(PlayerSession.CARRIER_STATS));

            case QUERY_TELL_ME_YOUR_NAME:
                String voiceName = systemSession.getAIVoice().getName();
                JsonObject response = new JsonObject();
                response.addProperty("response_text", "My name is " + voiceName + ", My Lord. How may I assist?");
                return GsonFactory.getGson().toJson(response);

            default:
                return null;
        }
    }

    private String getRoute() {
        StringBuilder sb = new StringBuilder();
        Collection<NavRouteDto> values = SystemSession.getInstance().getRoute().values();
        sb.append("[");
        for (NavRouteDto navRouteDto : values) {
            sb.append(navRouteDto.toJson()).append(", ");
        }
        sb.append("]");
        String result = sb.toString();
        return result.replace(", ]", "]");
    }
}
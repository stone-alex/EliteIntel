package elite.companion.comms.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.comms.ai.GrokAnalysisEndpoint;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.events.NavRouteDto;
import elite.companion.session.SystemSession;

import java.util.Collection;

import static elite.companion.session.SystemSession.CURRENT_SYSTEM;

public class AnalyzeDataHandler implements QueryHandler {

    @Override
    public String handle(String action, JsonObject params, String originalUserInput) {

        QueryAction query = findQuery(action);
        String dataJson = fetchDataForAction(query);
        if (dataJson == null || dataJson.isEmpty()) {
            return "No data available, My Lord.";
        }

        GrokAnalysisEndpoint grokAnalysisEndpoint = GrokAnalysisEndpoint.getInstance();
        // Send to Grok for analysis
        String analysisJson = grokAnalysisEndpoint.analyzeData(originalUserInput, dataJson);
        JsonObject analysis = new Gson().fromJson(analysisJson, JsonObject.class);
        return analysis.get("response_text").getAsString(); // Return for TTS
    }

    private QueryAction findQuery(String action) {
        QueryAction[] actions = QueryAction.values();
        for (QueryAction qa : actions) {
            if (qa.getAction().equals(action)) {
                return qa;
            }
        }
        VoiceGenerator.getInstance().speak("Sorry, no query action found for: "+action);
        throw new IllegalArgumentException("No query action found for: "+action);
    }


    private String fetchDataForAction(QueryAction action) {
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.getObject(CURRENT_SYSTEM);

        switch (action) {
            case QUERY_SEARCH_SIGNAL_DATA:
                return new Gson().toJson(String.valueOf(systemSession.getSignals()));
            case QUERY_SHIP_LOADOUT:
                return String.valueOf(systemSession.getObject(SystemSession.SHIP_LOADOUT_JSON));
                
            case QUERY_ANALYZE_ROUTE:
                return getRoute();
            // Add other queries...
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

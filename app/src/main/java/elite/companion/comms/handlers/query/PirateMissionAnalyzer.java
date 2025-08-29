package elite.companion.comms.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.comms.ai.GrokAnalysisEndpoint;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.session.SystemSession;

public class PirateMissionAnalyzer implements QueryHandler {
    @Override
    public String handle(String action, JsonObject params, String originalUserInput) {
        QueryActions query = findQuery(action);
        String dataJson = fetchDataForAction();
        if (dataJson == null || dataJson.equals("{\"missions\":[],\"bounties\":[]}") || dataJson.equals("{\"missions\":[]")) {
            return "No active pirate massacre missions detected, My Lord.";
        }

        GrokAnalysisEndpoint grokAnalysisEndpoint = GrokAnalysisEndpoint.getInstance();
        String prompt = buildPrompt(query, originalUserInput, dataJson);
        String analysisJson = grokAnalysisEndpoint.analyzeData(originalUserInput, prompt);
        JsonObject analysis = new Gson().fromJson(analysisJson, JsonObject.class);
        return analysis.get("response_text").getAsString();
    }

    private QueryActions findQuery(String action) {
        for (QueryActions qa : QueryActions.values()) {
            if (qa.getAction().equals(action)) {
                return qa;
            }
        }
        VoiceGenerator.getInstance().speak("Sorry, no query action found for: " + action);
        throw new IllegalArgumentException("No query action found for: " + action);
    }

    private String fetchDataForAction() {
        SystemSession session = SystemSession.getInstance();
        String missions = session.getPirateMissionsJson();
        String bounties = session.getPirateBountiesJson();
        return "{\"missions\":" + missions + ",\"bounties\":" + bounties + "}";
    }

    private String buildPrompt(QueryActions query, String userInput, String dataJson) {
        String basePrompt = "Analyze Elite Dangerous pirate massacre data. Group missions by TargetFaction for stacking. " +
                "Compute remaining kills as max(KillCount) per faction minus count of matching VictimFaction bounties. " +
                "Potential mission profit is sum of Rewards. Bounties collected is sum of TotalRewards for matching bounties. " +
                "Ignore expired missions. Data: " + dataJson + "\nUser query: " + userInput + "\n";

        switch (query) {
            case QUERY_PIRATE_KILLS_REMAINING:
                return basePrompt + "Respond with only the number of kills remaining per TargetFaction, formatted as: '[Faction]: [Kills] kills left.'";
            case QUERY_PIRATE_MISSION_PROFIT:
                return basePrompt + "Respond with only the total potential mission profit in credits, formatted as: 'Potential profit: [Credits] credits.'";
            case QUERY_PIRATE_STATUS:
                return basePrompt + "Provide a full summary including kills remaining, potential profit, and bounties collected per TargetFaction, formatted clearly.";
            default:
                return basePrompt;
        }
    }
}
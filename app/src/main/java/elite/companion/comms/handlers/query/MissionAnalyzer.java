package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.comms.ai.AIContextFactory;
import elite.companion.comms.ai.GrokAnalysisEndpoint;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.session.PlayerSession;
import elite.companion.util.EventBusManager;

public class MissionAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) {
        QueryActions query = findQuery(action);
        String dataJson = fetchDataForAction();
        if (dataJson == null || dataJson.equals("{\"missions\":[],\"bounties\":[]}") || dataJson.equals("{\"missions\":[]")) {
            return GenericResponse.getInstance().genericResponse("No active missions detected");
        }

        GrokAnalysisEndpoint grokAnalysisEndpoint = GrokAnalysisEndpoint.getInstance();
        String prompt = buildPrompt(query, originalUserInput, dataJson);
        return grokAnalysisEndpoint.analyzeData(originalUserInput, prompt);
    }

    private QueryActions findQuery(String action) {
        for (QueryActions qa : QueryActions.values()) {
            if (qa.getAction().equals(action)) {
                return qa;
            }
        }
        EventBusManager.publish(new VoiceProcessEvent("Sorry, no query action found for: " + action));
        throw new IllegalArgumentException("No query action found for: " + action);
    }

    private String fetchDataForAction() {
        PlayerSession session = PlayerSession.getInstance();
        String missions = session.getMissionsJson();
        String bounties = session.getBountiesJson();
        String missionKills = session.getMissionKillsJson();
        String bountiesCollectedThisSession = session.getBountyCollectedThisSession() + "";
        return "{\"missions\":" + missions
                + ",\"missionKills\":" + missionKills
                + ",\"bounties\":" + bounties
                + ",\"bountiesPayOut\":" + bountiesCollectedThisSession + "}";
    }

    private String buildPrompt(QueryActions query, String userInput, String dataJson) {

        StringBuilder basePrompt = new StringBuilder();
        basePrompt.append("Analyze Elite Dangerous mission data. Group missions by TargetFaction for stacking. ");
        basePrompt.append("Compute remaining kills as max(KillCount) per faction minus count of missionKills. ");
        basePrompt.append("Potential mission profit is sum of Mission Rewards. Bounties collected is sum of TotalRewards for matching bounties. ");
        basePrompt.append("Start responses directly with the requested information, avoiding conversational fillers like 'noted,' 'well,' 'right,' 'understood,' or similar phrases. ");
        basePrompt.append("Spell out numerals in full words (e.g., 285 = two hundred and eighty-five, 27 = twenty-seven)");
        AIContextFactory.appendBehavior(basePrompt);
        basePrompt.append("Ignore expired missions. Data: ");
        basePrompt.append(dataJson);
        basePrompt.append("\nUser query: ");
        basePrompt.append(userInput);
        basePrompt.append("\n");

        switch (query) {
            case QUERY_MISSION_KILLS_REMAINING:
                return basePrompt.append("Respond with only the number of kills remaining per TargetFaction, formatted as: '[Faction]: [ kills] kills left.'").toString();
            case QUERY_MISSION_PROFIT:
                return basePrompt.append("Respond with only the total potential mission profit in credits, formatted as: 'Potential profit: [Credits] credits.'").toString();
            case QUERY_MISSION_STATUS:
                return basePrompt.append("Provide a full summary including kills remaining, potential profit, and bounties collected per TargetFaction, formatted clearly.").toString();
            default:
                return basePrompt.toString();
        }
    }
}
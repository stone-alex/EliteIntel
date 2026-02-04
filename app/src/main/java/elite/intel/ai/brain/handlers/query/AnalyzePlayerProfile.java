package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class AnalyzePlayerProfile extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing commander's  data... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();

        String highestMilitaryRank = playerSession.getPlayerHighestMilitaryRank();
        long totalBountiesCollected = playerSession.getBountyCollectedLiveTime();
        long highestTransaction = playerSession.getHighestTransaction();
        long tradeProfits = playerSession.getTradeProfits();
        long personalCredits = playerSession.getPersonalCredits();
        double totalDistanceTraveled = playerSession.getTotalDistanceTraveled();
        long totalHyperspaceDistance = playerSession.getTotalHyperspaceDistance();
        long totalProfitsFromExploration = playerSession.getTotalProfitsFromExploration();
        long totalSystemsVisited = playerSession.getTotalSystemsVisited();
        long totalExobiologyProfits = playerSession.getTotalExobiologyProfits();

        RankAndProgressDto data = playerSession.getRankAndProgressDto();
        if (data == null) {
            return GenericResponse.getInstance().genericResponse("No data available");
        }

        String instructions = """
                Use this data to provide user with the current progression statistics.
                If a specific question is asked, provide a detailed answer using this data only.
                If no question is asked, provide a summary of the player's progression.
                    - timePledge provided in seconds, convert it to hours and minutes.
                    - powerRank is just a numeric value
                    - all other ranks are provided in percentage, 0 means no progress, 99 means 99% to next rank.
                """;
        return process(
                new AiDataStruct(
                        instructions,
                        new DataDto(
                                data,
                                highestMilitaryRank,
                                totalBountiesCollected,
                                highestTransaction,
                                tradeProfits,
                                personalCredits,
                                totalDistanceTraveled,
                                totalHyperspaceDistance,
                                totalSystemsVisited,
                                totalExobiologyProfits,
                                totalProfitsFromExploration
                        )
                ),
                originalUserInput
        );
    }

    record DataDto(
            RankAndProgressDto data,
            String highestMilitaryRank,
            long totalBountiesCollected,
            long highestTransaction,
            long totalProfits,
            long personalCredits,
            double totalDistanceTraveled,
            long totalHyperspaceDistanceTraveled,
            long totalSystemsVisited,
            long totalExobiologyProfits,
            long totalProfitsFromExploration
    ) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

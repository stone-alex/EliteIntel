package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.ai.brain.handlers.query.Queries.QUERY_PLAYER_STATS_ANALYSIS;

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

        return process(
                new AiDataStruct(
                        QUERY_PLAYER_STATS_ANALYSIS.getInstructions(),
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
    ) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

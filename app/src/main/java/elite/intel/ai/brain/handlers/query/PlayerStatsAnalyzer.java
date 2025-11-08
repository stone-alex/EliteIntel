package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.RankAndProgressDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static elite.intel.ai.brain.handlers.query.Queries.QUERY_PLAYER_STATS_ANALYSIS;

public class PlayerStatsAnalyzer extends BaseQueryAnalyzer implements QueryHandler {

    private static final Logger log = LogManager.getLogger(PlayerStatsAnalyzer.class);


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing commander's  data... stand by..."));
        RankAndProgressDto data = PlayerSession.getInstance().getRankAndProgressDto();
        if (data == null) {
            return GenericResponse.getInstance().genericResponse("No data available");
        }

        return process(new AiDataStruct(QUERY_PLAYER_STATS_ANALYSIS.getInstructions(), new DataDto(data)), originalUserInput);
    }

    record DataDto(RankAndProgressDto data) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

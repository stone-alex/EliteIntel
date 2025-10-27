package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_SCAN;

public class AnalyzeLastScanHandler  extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto lastScan = playerSession.getLastScan();

        return process(new AiDataStruct(ANALYZE_SCAN.getInstructions(), new DataDto(lastScan)), originalUserInput);
    }

    record DataDto(LocationDto lastScan) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

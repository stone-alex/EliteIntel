package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeLastScanHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing scanner... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto lastScan = playerSession.getLastScan();

        String instructions = """
                use this data to answer questions
                
                """;
        return process(new AiDataStruct(instructions, new DataDto(lastScan)), originalUserInput);
    }

    record DataDto(LocationDto lastScan) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

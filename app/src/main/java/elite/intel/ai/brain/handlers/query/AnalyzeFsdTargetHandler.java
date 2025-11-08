package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class AnalyzeFsdTargetHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing FSD telemetry... stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        String fsdTarget = playerSession.getFsdTarget();
        String data = fsdTarget != null ? toJson(fsdTarget) : toJson(" no information available...");
        return process(new AiDataStruct("Use this data to provide answers for the currently selected FSD target", new DataDto(data)), originalUserInput);
    }

    record DataDto(String data) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class AnalyzeFsdTargetHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing FSD telemetry. Stand by."));
        PlayerSession playerSession = PlayerSession.getInstance();

        ToJsonConvertible fsdTarget = playerSession.getFsdTarget();
        if (fsdTarget == null) {
            return process("No FSD target data available.");
        }

        String instructions = """
                Answer the user's question about the current FSD jump target.
                
                Data fields:
                - name: target star system name
                - fuelStarStatus: whether the target star can be used for fuel scooping
                - deathsDto: historical death statistics for the target system
                - trafficDto: historical traffic statistics for the target system
                - location: location data for the target system (star class, security, economy, etc.)
                - systemDto: additional star system data
                
                Rules:
                - Answer only what the user asked.
                - If asked about fuel scooping: use fuelStarStatus directly.
                - If any requested data is missing, say so.
                """;
        return process(new AiDataStruct(instructions, new DataDto(fsdTarget)), originalUserInput);
    }

    record DataDto(ToJsonConvertible data) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

}

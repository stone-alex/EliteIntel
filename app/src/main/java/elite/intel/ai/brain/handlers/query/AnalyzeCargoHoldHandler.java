package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

public class AnalyzeCargoHoldHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        //EventBusManager.publish(new AiVoxResponseEvent("Analyzing cargo data. Stand by."));
        PlayerSession playerSession = PlayerSession.getInstance();

        String instructions = """
                Answer the user's question about cargo.
                
                Data fields:
                - cargoCapacity: maximum cargo space in tons
                - cargo: items currently in the cargo hold (1 unit = 1 ton)
                
                Rules:
                - If asked about cargo contents: list items from cargo. If empty, say cargo hold is empty.
                - List items in cargo hold and number of units (tonnes).
                - If asked about cargo capacity: state the cargoCapacity value in tons.
                - No follow-up questions.
                """;
        return process(new AiDataStruct(instructions, new DataDto(playerSession.getShipLoadout().getCargoCapacity(), playerSession.getShipCargo())), originalUserInput);
    }

    record DataDto(int cargoCapacity, GameEvents.CargoEvent cargo) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

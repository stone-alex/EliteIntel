package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.shiploadout.ModuleDto;
import elite.intel.gameapi.journal.events.dto.shiploadout.ShipLoadOutDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.HashMap;
import java.util.Map;

import static elite.intel.ai.brain.handlers.query.Queries.QUERY_SHIP_LOADOUT;

public class AnalyzeShipLoadoutHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing ship loadout... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        ShipLoadOutDto shipLoadout = playerSession.getShipLoadout();
        if (shipLoadout == null) return process("No data available");

        return process(new AiDataStruct(QUERY_SHIP_LOADOUT.getInstructions(), new DataDto(ShipFactsExtractor.extractFacts(shipLoadout))), originalUserInput);
    }

    record DataDto(Map<String, Object> data) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    public class ShipFactsExtractor {
        public static Map<String, Object> extractFacts(ShipLoadOutDto loadout) {
            Map<String, Object> facts = new HashMap<>();
            if (loadout == null) {
                return facts;
            }

            facts.put("shipMake", loadout.getShipMake());
            facts.put("shipName", loadout.getShipName());
            facts.put("shipIdent", loadout.getShipIdent());
            facts.put("maxJumpRange", loadout.getMaxJumpRange());
            facts.put("cargoCapacity", loadout.getCargoCapacity());
            facts.put("unladenMass", loadout.getUnladenMass());

            if (loadout.getModules() != null) {
                for (ModuleDto module : loadout.getModules()) {
                    facts.put(module.getSlot(), module.getItem());
                }
            }
            return facts;
        }
    }
}

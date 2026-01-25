package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.shiploadout.EngineeringDto;
import elite.intel.gameapi.journal.events.dto.shiploadout.ModuleDto;
import elite.intel.gameapi.journal.events.dto.shiploadout.ShipLoadOutDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.HashMap;
import java.util.Map;

public class AnalyzeShipLoadoutHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing ship loadout... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        ShipLoadOutDto shipLoadout = playerSession.getShipLoadout();
        if (shipLoadout == null) return process("No data available");

        String instructions = """
                Provide answers about ship loadout details, health, damage report, suitability for a task, or whatever else user asks.
                IF damage detected (moduleHealthPercentage < 100) list damaged modules and damage percentage.
                IF asked about a specific module equiped reply with yes, or no. If yes, list know smodule specification.
                ______________________________________________________________
                For questions about ship classifications/suitability use this as a general guide line:
                Ship configuration builds/types:
                    - Discovery class ships: long range jumps (more than 50ly) light, no cargo, no weapons minimal shielding.
                    - Cargo class ships: a lot of cargo space, average jump range (less than 50 light years) may have some defencive weapons minimal shielding.
                    - Combat class ships: lazers / cannons / missiles, and massive shields are priority over cargo space or range
                    - Mining class ships: priority is mining tools, refinery and cargo space.
                ______________________________________________________________
                """;

        return process(new AiDataStruct(instructions, new DataDto(ShipFactsExtractor.extractFacts(shipLoadout))), originalUserInput);
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
                    facts.put(module.getSlot(), new ShipModule(module.getItem(), module.getHealthPercentage(), module.getEngineering()));
                }
            }
            return facts;
        }
    }

    record ShipModule(String moduleName, double moduleHealthPercentage, EngineeringDto engineering){
        @Override public String toString() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

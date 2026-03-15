package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.shiploadout.EngineeringDto;
import elite.intel.gameapi.journal.events.dto.shiploadout.ModuleDto;
import elite.intel.gameapi.journal.events.dto.shiploadout.ShipLoadOutDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.HashMap;
import java.util.Map;

public class AnalyzeShipLoadoutHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing loadout. Stand by."));
        PlayerSession playerSession = PlayerSession.getInstance();
        ShipLoadOutDto shipLoadout = playerSession.getShipLoadout();
        if (shipLoadout == null) return process("No data available");

        Map<String, Object> facts = ShipFactsExtractor.extractFacts(shipLoadout);

        Map<String, Float> damagedModules = new HashMap<>();
        boolean hasEngineeredModules = false;
        java.util.List<ModuleDto> modules = shipLoadout.getModules() != null ? shipLoadout.getModules() : java.util.Collections.emptyList();
        for (ModuleDto module : modules) {
            if (module.getHealthPercentage() < 100.0f) {
                damagedModules.put(module.getSlot(), module.getHealthPercentage());
            }
            if (module.getEngineering() != null) {
                hasEngineeredModules = true;
            }
        }

        String instructions = """
                Answer the user's question about ship loadout, health, or capabilities.
                
                Data fields:
                - data: map of ship modules by slot (moduleName, moduleHealthPercentage, engineering)
                - damagedModules: pre-computed map of slots with health below 100 percent (slot -> healthPercentage)
                - hasEngineeredModules: true if any module has engineering modifications
                
                Rules:
                - If asked about a specific module: check data by slot name, reply yes/no and list its specs if present.
                - If asked about damage: use damagedModules directly. If empty, report no damage detected.
                - If asked about engineering: use hasEngineeredModules. Engineered modules have modified stats or special bonuses.
                - If asked about ship classification or suitability, use these guidelines:
                  - Discovery: jump range above fifty light years, light build, no cargo, minimal weapons and shields
                  - Cargo: large cargo space, jump range below fifty light years, minimal weapons
                  - Combat: weapons and shields are priority, cargo and range are secondary
                  - Mining: mining tools, refinery, and cargo space are priority
                - Answer only what was asked.
                """;

        return process(new AiDataStruct(instructions, new DataDto(facts, damagedModules, hasEngineeredModules)), originalUserInput);
    }

    record DataDto(Map<String, Object> data, Map<String, Float> damagedModules,
                   boolean hasEngineeredModules) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

    public static class ShipFactsExtractor {
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
                    StringBuilder sb = new StringBuilder();
                    EngineeringDto engineering = module.getEngineering();
                    if (engineering != null) {
                        String bluPrintName = engineering.getBlueprintName();
                        sb.append("Engineering: ").append(bluPrintName).append(". ");
                        sb.append(" Modifiers: ");
                        engineering.getModifiers().forEach(data -> {
                            sb.append(data.getLabel()).append(", ");
                        });
                    }
                    facts.put(module.getSlot(), new ShipModule(module.getItem(), module.getHealthPercentage(), sb.toString()));
                }
            }
            return facts;
        }
    }

    record ShipModule(String moduleName, double moduleHealthPercentage, String engineering) implements ToYamlConvertable {

        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

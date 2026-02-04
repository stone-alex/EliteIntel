package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.gameapi.journal.events.dto.MaterialDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeMaterialsOnPlanetHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing geological data... Stand by..."));

        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());
        if (currentLocation.getBodyId() < 0) return process("No location data available");

        List<MaterialDto> materials = currentLocation.getMaterials();
        String instructions = """
                You are a strict material list processor for planetary raw materials.
                
                Rules (must follow exactly):
                - Use ONLY the "materials" array from the user message JSON
                - Each entry has "material" (name) and "rarity" (percentage float)
                - Rarer materials = LOWER rarity percentage
                - More common materials = HIGHER rarity percentage
                - Never invent, guess, calculate, or use external knowledge
                - Never assume any field like topRare exists — it does NOT
                
                How to answer common patterns (be extremely brief):
                - "X most rare / rarest / top X rare materials" → sort by rarity ASC, take first X names
                - "X most common materials"                        → sort by rarity DESC, take first X names
                - "most / least common materials"                  → return top 3 (most common) and bottom 3 (least common / rarest)
                - "does it have X / is X present / can I find X"   → check if material name exists (case-insensitive), answer "Yes" or "No"
                - "how much / percentage of X"                      → return the rarity value formatted to exactly 2 decimal places + "%" if present, else "No Data Available."
                - "list all materials"                             → return all material names (comma-separated or line-separated)
                
                Output only the requested answer text — no extra words, no explanations, no JSON inside the answer.
                """;

        if (materials.isEmpty()) {
            return process(" no materials data available...");
        } else {
            return process(new AiDataStruct(instructions, new DataDto(toMaterialData(materials))), originalUserInput);
        }
    }

    private List<MaterialData> toMaterialData(List<MaterialDto> materials) {
        ArrayList<MaterialData> data = new ArrayList<>();
        for (MaterialDto m : materials) {
            data.add(new MaterialData(m.getMaterialName(), Math.round(m.getMaterialPercentage() * 100.0) / 100.0));
        }
        return data;
    }

    record MaterialData(String material, double rarity) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

    record DataDto(List<MaterialData> materials) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }
}

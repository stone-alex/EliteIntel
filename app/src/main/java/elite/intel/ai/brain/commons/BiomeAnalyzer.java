package elite.intel.ai.brain.commons;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.handlers.query.BaseQueryAnalyzer;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.DiscoveryAnnouncementEvent;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import elite.intel.util.yaml.ToYamlConvertable;
import elite.intel.util.yaml.YamlFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BiomeAnalyzer extends BaseQueryAnalyzer {

    private static BiomeAnalyzer instance;
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    private BiomeAnalyzer() {
    }

    public static BiomeAnalyzer getInstance() {
        if (instance == null) {
            instance = new BiomeAnalyzer();
        }
        return instance;
    }

    public JsonObject analyzeBiome(@Nullable String originalUserInput, LocationData... locations) {
        if (locations == null) {
            JsonObject object = new JsonObject();
            object.addProperty("data", "No locations provided");
            return object;
        }


        String instructions = """
                You are a classifier. IGNORE all planet properties except planetShortName, planetClass, atmosphere, temperature, volcanism (if relevant) — but ONLY to match against PROBABLE genusToBiome map.
                
                ONLY source: the provided genusToBiome map.
                
                For each planet:
                - Find ALL genera whose conditions can POSSIBLY be met (lenient/partial match allowed, do NOT require 100% fit).
                - Output: "Planet <planetName>: " followed by comma-separated list (no trailing comma).
                - Number of genera listed MUST be equal or greater than numBioSignals.
                - If ZERO plausible matches but numBioSignals > 0 → list "Bacterium"
                - If no POSSIBLE/PROBABLE matches and numBioSignals = 0 → "Planet <shortName>: no matching genus found"
                - Pure text only. No explanations, no stats, no other words.
                - numBioSignals indicates number of detected bio signals. There should be at least that many different matches
                
                Output format: { "type":"chat", "response_text": "Planet X: probable genus are Genus1, Genus2, ... Genus5 Planet Y: Genus1, Genus2, ..., Genus5" }
                """;

        List<LocationData> list = List.of(locations);
        Map<Long, LocationDto> byPrimaryStar = locationManager.findByPrimaryStar(playerSession.getPrimaryStarName());
        return process(new AiDataStruct(instructions, new DataDto2(BioForms.getGenusToBiome(), list, starSystemCharacteristics(byPrimaryStar.values()))), originalUserInput);
    }

    private String starSystemCharacteristics(Collection<LocationDto> starSystem) {
        StringBuilder sb = new StringBuilder();
        sb.append("System:");
        for (LocationDto stellarObject : starSystem) {
            if (stellarObject.getStarClass() == null && stellarObject.getPlanetClass() != null) {
                sb.append(" has planet type:'").append(stellarObject.getPlanetClass()).append("' distance:").append(stellarObject.getDistance()).append(", ");
            } else if (stellarObject.getStarClass() != null) {
                sb.append("Star type").append(stellarObject.getBodyType()).append(" present, ");
            }
        }
        sb.append(" .");

        return sb.toString();
    }

    public record LocationData(String planetName, int numBioSignals, String planetClass, String distance, String vulcanism, String atmosphere, String temperature) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

    record DataDto2(Map<String, String> genusToBiome, Collection<LocationData> locations, String starSystemCharacteristics) implements ToYamlConvertable {
        @Override public String toYaml() {
            return YamlFactory.toYaml(this);
        }
    }

}

package elite.intel.ai.brain.commons;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.BaseQueryAnalyzer;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
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
                Predict probable genus for each planet using the genusToBiome map.
                
                Data fields:
                - locations: list of planets to classify (planetName, planetClass, atmosphere, temperature in Kelvin, vulcanism, numBioSignals)
                - genusToBiome: map of genus name to the biome conditions it requires
                - starSystemCharacteristics: star types and planet types present in this system
                
                Rules:
                - For each planet: match its planetClass, atmosphere, temperature, and vulcanism against genusToBiome conditions. Allow partial and lenient matches.
                - The number of genus listed per planet must be equal to or greater than numBioSignals.
                - If no plausible matches exist but numBioSignals is greater than zero: list Bacterium as a fallback.
                - If no matches and numBioSignals is zero: output "Planet <planetName>: no matching genus found".
                - Output only planet names and genus lists. No explanations.
                
                Output format: Planet X: Possible genus are Genus1, Genus2, Genus3. Planet Y: Possible genus are Genus1, Genus2.
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

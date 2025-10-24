package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

import java.util.Collection;
import java.util.Map;

public class PlanetBiomeAnalyzerHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();

        String instructions = "Parse the genusToBiome map, where each value is formatted as 'Planet:<types>|Atmosphere:<types>|Gravity:<constraint>|Temperature:<range>|Volcanism:<types>|System:<constraints>'. Parse locations list to find planets with bioSignals present. Using the location's atmosphere, gravity, temperature, volcanism, and system details, identify genera whose biome conditions match. Return a list of probable matching genera, for planet(s) that have bioSignals prioritizing specific matches over broad ones. If more than one planet, provide planetShortName along with the genus.";

        Map<Long, LocationDto> locations = playerSession.getLocations();

        return process(new DataDto(BioForms.getGenusToBiome(), locations.values(), instructions), originalUserInput);
    }

    record DataDto(Map<String, String> genusToBiome, Collection<LocationDto> locations, String instructions) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }

}

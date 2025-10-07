package elite.intel.ai.brain.commons;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.AIConstants;
import elite.intel.ai.brain.handlers.query.BaseQueryAnalyzer;
import elite.intel.ai.mouth.subscribers.events.DiscoveryAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.dto.GenusDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;

import java.util.List;
import java.util.Map;

public class BiomeAnalyzer extends BaseQueryAnalyzer {

    public void analyzeBiome(LocationDto location) {
        if (location == null || LocationDto.LocationType.PLANET_OR_MOON != location.getLocationType()) {
            return;
        }

        List<GenusDto> genus = location.getGenus();
        if(genus == null || genus.isEmpty()) {
            String instructions = "Parse the genusToBiome map, where each value is formatted as 'Planet:<types>|Atmosphere:<types>|Gravity:<constraint>|Temperature:<range>|Volcanism:<types>|System:<constraints>'. Using the location's atmosphere, gravity, temperature, volcanism, and system details, identify genera whose biome conditions match. Return a list of matching genera, prioritizing specific matches over broad ones (e.g., exclude Bacterium unless no other matches).";
            JsonObject jsonObject = analyzeData(new DataDto(BioForms.getGenusToBiome(), location, instructions).toString(), "What genus might be present on this world?");
            String responseTextToUse = jsonObject.has(AIConstants.PROPERTY_RESPONSE_TEXT)
                    ? jsonObject.get(AIConstants.PROPERTY_RESPONSE_TEXT).getAsString()
                    : null;
            if (responseTextToUse != null && !responseTextToUse.isEmpty()) {
                EventBusManager.publish(new DiscoveryAnnouncementEvent(responseTextToUse));
            }
        }
    }

    record DataDto(Map<String, String> genusToBiome, LocationDto location, String instructions) {
    }

}

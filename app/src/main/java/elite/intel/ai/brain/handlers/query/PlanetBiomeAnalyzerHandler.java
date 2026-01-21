package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public class PlanetBiomeAnalyzerHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing planetary and biome data... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
        String instructions = """
                You are a pure classifier. IGNORE ALL PLANET PROPERTIES except planetShortName.
                
                Compare location data to genusToBiome map to find possible genus.
                
                ONLY use the "genusToBiome" map to determine possible genus.
                Consider genus where ALL listed conditions are POSSIBLY met (do not require perfect match).
                
                Rules you MUST follow:
                - Use ONLY genus names from the map
                - Most specific genus first
                - Comma separated, no trailing comma
                - If zero genera match → Planet <planetShortName>: no matching genus found
                - No descriptions, no stats, no temperature, no gravity, no atmosphere words, no explanations.
                                
                Correct response examples:
                match found: { \"type\":\"chat\", \"response_text\": \"Planet 12 d: Aleoida, Bacterium, Osseus\"}
                or no match found { \"type\":\"chat\", \"response_text\": \"Planet ZZ 3: none\"}
                """;

        Map<Long, LocationDto> locations = playerSession.getLocations();
        String planetName = params.get("key").getAsString().replace(" ", "");

        if (SystemSession.getInstance().isRunningLocalLLM()) {
            LocationDto location = findFirstMatchingLocation(locations, planetName);
            if (location != null) {
                return process(new AiDataStruct(instructions, new DataDto1(BioForms.getGenusToBiome(), location)), originalUserInput);
            }

            location = findFirstWithBioSignals(locations);
            if (location != null) {
                return process(new AiDataStruct(instructions, new DataDto1(BioForms.getGenusToBiome(), location)), originalUserInput);
            }
        } else {
            return process(new AiDataStruct(instructions, new DataDto2(BioForms.getGenusToBiome(), locations.values())), originalUserInput);
        }

        return process("planet " + planetName + " not found in data");
    }

    public LocationDto findFirstMatchingLocation(Map<Long, LocationDto> locations, String planetName) {
        if (planetName == null || planetName.trim().isEmpty()) {
            return null;
        }

        Optional<LocationDto> firstMatch = Optional.empty();
        for (LocationDto locationDto : locations.values()) {
            String lowerPlanetName = planetName.toLowerCase();
            String lowerShortName = trimToNull(locationDto.getPlanetShortName().toLowerCase());
            if (lowerShortName != null && lowerPlanetName.contains(lowerShortName) && planetName.length() > 0) {
                firstMatch = Optional.of(locationDto);
                break;
            }
        }

        return firstMatch.orElse(null);
    }

    public LocationDto findFirstWithBioSignals(Map<Long, LocationDto> locations) {
        for (LocationDto v : locations.values()) {
            if (v.getBioSignals() > 0) {
                return v;
            }
        }
        return null;
    }


    record DataDto1(Map<String, String> genusToBiome, LocationDto location) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

    record DataDto2(Map<String, String> genusToBiome, Collection<LocationDto> locations) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

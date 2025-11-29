package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.data.BioForms;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Collection;
import java.util.Map;

public class PlanetBiomeAnalyzerHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing planetary and biome data... Stand by..."));
        PlayerSession playerSession = PlayerSession.getInstance();
                String instructions = "DO NOT OUTPUT ANY PLANET STATS. From genusToBiome map and locations with bioSignals:true, match biome conditions only. Reply EXACTLY:\n" +
                "\n" +
                "Planet <planetShortName>: <Genus1>, <Genus2>, <Genus3> (most specific first)\n" +
                "\n" +
                "One line per planet. If user names a specific planet, return ONLY that planet's line. No temperature, gravity, atmosphere, volcanism, periods, or explanations ever. Day length question → \"XX hours YY minutes\" only.";

        Map<Long, LocationDto> locations = playerSession.getLocations();

        return process(new AiDataStruct(instructions, new DataDto(BioForms.getGenusToBiome(), locations.values())), originalUserInput);
    }

    record DataDto(Map<String, String> genusToBiome, Collection<LocationDto> locations) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzeGeologyInStarSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Searching for planets with geological signals..."));
        Map<String, Integer> planetsWithGeoSignals = planetsWithGeoSignals();
        String instructions = """
                    Data contains a map of planet name to amount of geo signals
                    Name the planets with geo signals if any.
                    Example: Planets with geological signals: 1A - 3 signals, 4C - 2 signals.
                """;
        return process(new AiDataStruct(instructions, new DataDto(planetsWithGeoSignals)), originalUserInput);
    }

    private Map<String, Integer> planetsWithGeoSignals() {
        Map<String, Integer> result = new HashMap<>();
        Map<Long, LocationDto> locations = playerSession.getLocations();
        for (LocationDto location : locations.values()) {
            if (location.getGeoSignals() > 0) {
                result.put(location.getPlanetShortName(), location.getGeoSignals());
            }
            if(location.getFssSignals() == null) continue;
            List<FSSBodySignalsEvent.Signal> fssSignals = location.getFssSignals();
            for(FSSBodySignalsEvent.Signal signal :fssSignals){
                if(signal.getTypeLocalised().toLowerCase().contains("geo")){
                    result.put(location.getPlanetShortName(), signal.getCount());
                }
            }
        }
        return result;
    }

    record DataDto(Map<String, Integer> planetsWithGeoSignals) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.FssSignalDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalyzeFssSignalsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        List<FSSBodySignalsEvent.Signal> fssBodySignals = playerSession.getCurrentLocation().getFssSignals();
        Set<FssSignalDto> detectedSignals = getDetectedSignals();
        String instructions = """
                use this data to provide answers about signals detected in this star system                
                """;
        return process(new AiDataStruct(instructions, new DataDto(fssBodySignals, detectedSignals)), originalUserInput);
    }

    private Set<FssSignalDto> getDetectedSignals() {
        Map<Long, LocationDto> locations = playerSession.getLocations();
        if (locations.isEmpty()) return new HashSet<>();
        for (LocationDto location : locations.values()) {
            if (LocationDto.LocationType.PRIMARY_STAR.equals(location.getLocationType())) {
                return location.getDetectedSignals();
            }
        }
        return new HashSet<>();
    }

    record DataDto(List<FSSBodySignalsEvent.Signal> fssBodySignals, Set<FssSignalDto> detectedSignals) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

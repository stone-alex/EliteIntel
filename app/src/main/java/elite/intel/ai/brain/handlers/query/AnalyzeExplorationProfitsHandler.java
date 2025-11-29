package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;
import java.util.Map;

import static elite.intel.ai.brain.handlers.query.Queries.ANALYZE_STAR_SYSTEM_EXPLORATION;

public class AnalyzeExplorationProfitsHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        EventBusManager.publish(new AiVoxResponseEvent("Analyzing exploration data... Stand by..."));

        PlayerSession playerSession = PlayerSession.getInstance();
        List<BioSampleDto> allCompletedBioSamples = playerSession.getBioCompletedSamples();
        Map<Long, LocationDto> planetsAndMoons = playerSession.getLocations();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        List<FSSBodySignalsEvent.Signal> fullSpectrumScanBodySignals = currentLocation.getFssSignals();
        return process(
                new AiDataStruct(
                        ANALYZE_STAR_SYSTEM_EXPLORATION.getInstructions(),
                        new DataDto(allCompletedBioSamples, planetsAndMoons, fullSpectrumScanBodySignals)),
                originalUserInput
        );
    }

    record DataDto(
            List<BioSampleDto> allCompletedBioSamples,
            Map<Long, LocationDto> planetsAndMoons,
            List<FSSBodySignalsEvent.Signal> fullSpectrumScanBodySignals
    ) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

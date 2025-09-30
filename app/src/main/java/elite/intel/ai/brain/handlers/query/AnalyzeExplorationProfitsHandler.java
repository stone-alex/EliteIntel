package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.List;
import java.util.Map;

public class AnalyzeExplorationProfitsHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        List<BioSampleDto> allCompletedBioSamples = playerSession.getBioCompletedSamples();
        Map<Long, LocationDto> planetsAndMoons = playerSession.getLocations();
        List<FSSBodySignalsEvent.Signal> fullSpectrumScanBodySignals = playerSession.getCurrentLocation().getFssSignals();
        return analyzeData(new DataDto(allCompletedBioSamples, planetsAndMoons, fullSpectrumScanBodySignals).toJson(), originalUserInput);
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

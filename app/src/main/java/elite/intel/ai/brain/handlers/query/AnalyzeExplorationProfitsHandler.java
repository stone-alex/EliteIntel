package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

import java.util.List;
import java.util.Map;

public class AnalyzeExplorationProfitsHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        List<BioSampleDto> allCompletedBioSamples = playerSession.getBioCompletedSamples();
        Map<Long, LocationDto> planetsAndMoons = playerSession.getLocations();
        List<FSSBodySignalsEvent.Signal> fullSpectrumScanBodySignals = playerSession.getCurrentLocation().getFssSignals();
        String instructions = "Use this data to provide answers on potential exo-biology exploration profits.";
        return process(new DataDto(instructions, allCompletedBioSamples, planetsAndMoons, fullSpectrumScanBodySignals), originalUserInput);
    }

    record DataDto(
            String instructions,
            List<BioSampleDto> allCompletedBioSamples,
            Map<Long, LocationDto> planetsAndMoons,
            List<FSSBodySignalsEvent.Signal> fullSpectrumScanBodySignals
    ) implements AiData {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }

        @Override public String getInstructions() {
            return instructions;
        }
    }
}

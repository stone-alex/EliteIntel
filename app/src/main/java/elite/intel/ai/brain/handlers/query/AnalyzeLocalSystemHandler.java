package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.StarSystemDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class AnalyzeLocalSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        //QueryActions query = findQuery(action);
        PlayerSession playerSession = PlayerSession.getInstance();

        LocationDto currentLocation = playerSession.getCurrentLocation();

        StarSystemDto localSystemDto = EdsmApiClient.searchStarSystem(currentLocation.getStarName(), 1);
        String completeData = localSystemDto.toJson();

        if (localSystemDto.getData() == null) {
            EventBusManager.publish(new VoiceProcessEvent("Limited data available..."));
            return analyzeData(currentLocation.toJson(), originalUserInput);
        } else {
            return analyzeData(completeData, originalUserInput);
        }

    }
}

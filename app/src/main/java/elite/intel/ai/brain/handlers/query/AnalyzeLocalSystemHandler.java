package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.ai.search.api.EdsmApiClient;
import elite.intel.ai.search.api.dto.StarSystemDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

public class AnalyzeLocalSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Logger log = LogManager.getLogger(AnalyzeLocalSystemHandler.class);
    private static final Gson GSON = GsonFactory.getGson();


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

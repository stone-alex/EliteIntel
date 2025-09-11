package elite.companion.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.ai.search.api.EdsmApiClient;
import elite.companion.ai.search.api.dto.StarSystemDto;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.dto.LocationDto;
import elite.companion.session.PlayerSession;
import elite.companion.util.json.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzeLocalSystemHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeLocalSystemHandler.class);
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

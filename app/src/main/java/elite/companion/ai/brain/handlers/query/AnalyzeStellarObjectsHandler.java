package elite.companion.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.ai.search.api.EdsmApiClient;
import elite.companion.ai.search.api.dto.SystemBodiesDto;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.dto.StellarObjectDto;
import elite.companion.session.PlayerSession;
import elite.companion.util.json.GsonFactory;

import java.util.Map;

public class AnalyzeStellarObjectsHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Gson GSON = GsonFactory.getGson();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        PlayerSession playerSession = PlayerSession.getInstance();

        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(String.valueOf(playerSession.get(PlayerSession.CURRENT_SYSTEM_NAME)));
        if (systemBodiesDto.getData() == null && systemBodiesDto.getData().getBodies() != null) {
            EventBusManager.publish(new VoiceProcessEvent("Limited data available..."));
            String dataJsonStr = getData();
            if (dataJsonStr == null || dataJsonStr.isEmpty()) {
                return GenericResponse.getInstance().genericResponse("No data available");
            }
            return analyzeData(dataJsonStr, originalUserInput);
        } else {
            return analyzeData(systemBodiesDto.toJson(), originalUserInput);
        }


    }

    private String getData() {
        Map<String, StellarObjectDto> data = PlayerSession.getInstance().getStellarObjects();
        return data != null ? GSON.toJson(data) : null;
    }

}

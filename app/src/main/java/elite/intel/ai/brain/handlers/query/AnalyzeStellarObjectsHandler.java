package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.SystemBodiesDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;

import java.util.Map;

public class AnalyzeStellarObjectsHandler extends BaseQueryAnalyzer implements QueryHandler {

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
        return data != null ? toJson(data) : null;
    }

}

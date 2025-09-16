package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.ai.search.api.EdsmApiClient;
import elite.intel.ai.search.api.dto.SystemBodiesDto;
import elite.intel.ai.search.api.dto.data.BodyData;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

import java.util.Optional;

public class AnalyzeMaterialsOnPlanetHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        String starSystemName = String.valueOf(playerSession.get(PlayerSession.CURRENT_SYSTEM_NAME));
        String bodyName = String.valueOf(playerSession.get(PlayerSession.LANDED_ON_BODY));

        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(starSystemName);
        Optional<BodyData> bodyData = systemBodiesDto.getData().getBodies().stream().filter(
                body -> body.getName().equals(bodyName)
        ).findFirst();

        StringBuilder sb = new StringBuilder();
        bodyData.map(BodyData::getMaterials).ifPresent(materials -> {
            if (!materials.isEmpty()) {
                materials.forEach((key, value) -> {
                    sb.append(key).append(": ").append(value).append(" percent ").append(". ");
                });
            }
        });
        String data = toJson(sb.toString());
        if (!data.isEmpty()) return analyzeData(data, originalUserInput);
        else return analyzeData(toJson(" no market data available..."), originalUserInput);
    }
}

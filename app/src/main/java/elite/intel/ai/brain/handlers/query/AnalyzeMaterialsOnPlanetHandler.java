package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.SystemBodiesDto;
import elite.intel.ai.search.edsm.dto.data.BodyData;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

import java.util.Optional;

public class AnalyzeMaterialsOnPlanetHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        String starSystemName = String.valueOf(playerSession.get(PlayerSession.CURRENT_SYSTEM_NAME));
        LocationDto currentLocation = playerSession.getCurrentLocation();

        if(currentLocation == null) return analyzeData(toJson("No data available"), originalUserInput);

        String bodyName = currentLocation.getPlanetName();

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

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.SystemBodiesDto;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Collection;
import java.util.Map;

public class AnalyzeStellarObjectsHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        PlayerSession playerSession = PlayerSession.getInstance();
        Map<String, StellarObjectDto> stellarObjects = playerSession.getStellarObjects();
        SystemBodiesDto systemBodiesDto = EdsmApiClient.searchSystemBodies(String.valueOf(playerSession.get(PlayerSession.CURRENT_SYSTEM_NAME)));

        return analyzeData(new DataDto(stellarObjects.values(), systemBodiesDto).toJson(), originalUserInput);
    }

    record DataDto(Collection<StellarObjectDto> stellarObjects, SystemBodiesDto systemBodiesDto) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

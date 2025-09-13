package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StationDataHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Logger log = LoggerFactory.getLogger(StationDataHandler.class);
    private static final Gson GSON = GsonFactory.getGson();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        QueryActions query = findQuery(action);
        PlayerSession playerSession = PlayerSession.getInstance();

        Object status = playerSession.get(PlayerSession.CURRENT_FUEL_STATUS);
        String dataJsonStr = status != null ? GSON.toJson(status) : null;

        // Check JSON validity
        if (!JsonDataFactory.getInstance().isValidJson(dataJsonStr)) {
            log.error("Invalid data JSON for query {}: {}", query, dataJsonStr);
            return GenericResponse.getInstance().genericResponse("Data error");
        }

        return analyzeData(dataJsonStr, originalUserInput);
    }
}

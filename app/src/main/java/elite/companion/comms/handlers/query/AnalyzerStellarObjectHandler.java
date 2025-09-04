package elite.companion.comms.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.companion.session.PlayerSession;
import elite.companion.util.GsonFactory;
import elite.companion.util.JsonDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.companion.comms.handlers.query.QueryActions.ANALYZE_SCAN;

public class AnalyzerStellarObjectHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Logger log = LoggerFactory.getLogger(AnalyzerStellarObjectHandler.class);
    private static final Gson GSON = GsonFactory.getGson();


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        String dataJsonStr = getData();

        if (dataJsonStr == null || dataJsonStr.isEmpty()) {
            return GenericResponse.getInstance().genericResponse("No data available");
        }

        // Check JSON validity
        if (!JsonDataFactory.getInstance().isValidJson(dataJsonStr)) {
            log.error("Invalid data JSON for query {}: {}", ANALYZE_SCAN.getAction(), dataJsonStr);
            return GenericResponse.getInstance().genericResponse("Data error");
        }

        return analyzeData(dataJsonStr, originalUserInput);

    }

    private String getData() {
        Object scan = PlayerSession.getInstance().get(PlayerSession.LAST_SCAN);
        return scan != null ? GSON.toJson(scan) : null;
    }
}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.JsonDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elite.intel.ai.brain.handlers.query.QueryActions.ANALYZE_SCAN;

/**
 * The `AnalyzerStellarObjectHandler` class is responsible for processing and
 * handling queries related to stellar object analysis. It extends `BaseQueryAnalyzer`
 * to use the core analysis functionality and implements the `QueryHandler` interface
 * to adhere to the contract for handling specific query actions.
 * <p>
 * This handler retrieves data from the player's session, validates the provided JSON,
 * and performs analysis on the data using an external analysis endpoint.
 * <p>
 * Primary responsibilities of this class include:
 * - Retrieving the latest scan data from the player's session.
 * - Validating the JSON structure of the retrieved data.
 * - Delegating the data to analysis logic to generate insights based on user queries.
 * <p>
 * Logging is employed to track and report any issues with invalid or missing data.
 */
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

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.gameapi.journal.events.dto.StellarObjectDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Map;

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

    private static final Logger log = LogManager.getLogger(AnalyzerStellarObjectHandler.class);


    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        return analyzeData(getData(), originalUserInput);
    }

    private String getData() {
        Object scan = PlayerSession.getInstance().get(PlayerSession.LAST_SCAN);
        return scan != null ? toJson(scan) : null;
    }
}

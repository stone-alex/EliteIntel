package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.about.EliteIntelFactory;
import elite.intel.util.json.JsonDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles queries related to the capabilities of the Elite AI.
 * This class extends {@code BaseQueryAnalyzer} to leverage pre-built query analysis methods
 * and implements the {@code QueryHandler} interface for structured query handling.
 * <p>
 * Core functionality:
 * - Retrieves the capabilities of the Elite AI  using {@code EliteIntelFactory}.
 * - Converts the capabilities data into a JSON string using {@code JsonDataFactory}.
 * - Validates the generated JSON string to ensure data integrity.
 * - Returns an analyzed response using {@code BaseQueryAnalyzer}'s analysis methods.
 * <p>
 * Exceptions:
 * - Throws {@code Exception} if an issue occurs during query handling.
 * <p>
 * Dependencies:
 * - {@code BaseQueryAnalyzer} for query analysis.
 * - {@code JsonDataFactory} for JSON serialization and validation.
 * - {@code EliteIntelFactory} for accessing AI capabilities.
 * - {@code GenericResponse} for returning appropriate responses in error scenarios.
 * <p>
 * Logging:
 * - Logs errors encountered during JSON validation or data processing.
 */
public class WhatAreYourCapabilitiesHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Logger log = LoggerFactory.getLogger(WhatAreYourCapabilitiesHandler.class);

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        String dataJsonStr = EliteIntelFactory.getInstance().getCapabilities().toJson();
        QueryActions query = findQuery(action);

        // Check JSON validity
        if (!JsonDataFactory.getInstance().isValidJson(dataJsonStr)) {
            log.error("Invalid data JSON for query {}: {}", query, dataJsonStr);
            return GenericResponse.getInstance().genericResponse("Data error");
        }

        return analyzeData(dataJsonStr, originalUserInput);
    }
}

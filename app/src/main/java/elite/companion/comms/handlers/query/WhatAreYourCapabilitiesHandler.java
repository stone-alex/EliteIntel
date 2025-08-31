package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.about.EliteCompanionFactory;
import elite.companion.util.JsonDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhatAreYourCapabilitiesHandler extends BaseQueryAnalyzer implements QueryHandler {

    private static final Logger log = LoggerFactory.getLogger(WhatAreYourCapabilitiesHandler.class);

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {

        String dataJsonStr = JsonDataFactory.getInstance().toJsonString(EliteCompanionFactory.getInstance().getCapabilities());
        QueryActions query = findQuery(action);

        // Check JSON validity
        if (!JsonDataFactory.getInstance().isValidJson(dataJsonStr)) {
            log.error("Invalid data JSON for query {}: {}", query, dataJsonStr);
            return GenericResponse.getInstance().genericResponse("Data error");
        }

        return analyzeData(dataJsonStr, originalUserInput);
    }
}

package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.about.EliteIntelFactory;
import elite.intel.about.dto.AICapabilitiesDto;

public class WhatAreYourCapabilitiesHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        AICapabilitiesDto dto = EliteIntelFactory.getInstance().getCapabilities();
        return analyzeData(toJson(dto), originalUserInput);
    }
}

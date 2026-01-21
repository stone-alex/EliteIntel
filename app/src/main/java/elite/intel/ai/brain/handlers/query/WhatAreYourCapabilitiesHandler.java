package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.query.struct.AiDataStruct;
import elite.intel.help.EliteIntelFactory;
import elite.intel.help.dto.AICapabilitiesDto;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class WhatAreYourCapabilitiesHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        AICapabilitiesDto capabilities = EliteIntelFactory.getInstance().getCapabilities();
        return process(new AiDataStruct("Summarize application capabilities.", new DataDto(capabilities)), originalUserInput);
    }

    record DataDto(AICapabilitiesDto capabilities) implements ToJsonConvertible {

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

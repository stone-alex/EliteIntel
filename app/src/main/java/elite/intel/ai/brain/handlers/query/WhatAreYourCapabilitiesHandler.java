package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.help.EliteIntelFactory;
import elite.intel.help.dto.AICapabilitiesDto;
import elite.intel.util.json.AiData;
import elite.intel.util.json.GsonFactory;

import static elite.intel.ai.brain.handlers.query.Queries.WHAT_ARE_YOUR_CAPABILITIES;

public class WhatAreYourCapabilitiesHandler extends BaseQueryAnalyzer implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        AICapabilitiesDto capabilities = EliteIntelFactory.getInstance().getCapabilities();
        return process(new DataDto(WHAT_ARE_YOUR_CAPABILITIES.getInstructions(), capabilities), originalUserInput);
    }

    record DataDto(String instructions, AICapabilitiesDto capabilities) implements AiData {
        @Override public String getInstructions() {
            return instructions;
        }

        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }

}

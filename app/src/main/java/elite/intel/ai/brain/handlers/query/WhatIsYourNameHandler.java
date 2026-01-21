package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.SystemSession;

public class WhatIsYourNameHandler implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        SystemSession systemSession = SystemSession.getInstance();
        return GenericResponse.getInstance().genericResponse("I am " + systemSession.getAIVoice().getName());
    }
}

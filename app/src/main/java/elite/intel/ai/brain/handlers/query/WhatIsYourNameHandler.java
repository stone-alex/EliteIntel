package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.session.SystemSession;

public class WhatIsYourNameHandler implements QueryHandler {

    private final SystemSession systemSession = SystemSession.getInstance();

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        return GenericResponse.getInstance().genericResponse(" I am " + aiName());
    }

    private String aiName() {
        return systemSession.useLocalTTS() ? "Amelia" : systemSession.getAIVoice().getName();
    }
}

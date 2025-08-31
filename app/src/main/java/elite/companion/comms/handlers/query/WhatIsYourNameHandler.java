package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.session.SystemSession;

public class WhatIsYourNameHandler implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        SystemSession systemSession = SystemSession.getInstance();
        return GenericResponse.getInstance().genericResponse("My name is " + systemSession.getAIVoice().getName());
    }
}

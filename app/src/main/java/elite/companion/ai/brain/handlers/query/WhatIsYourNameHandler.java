package elite.companion.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.companion.session.SystemSession;

/**
 * Handles the "What is your name?" query by providing the name of the AI.
 * <p>
 * This class implements the {@link QueryHandler} interface to process a specific user query
 * and return an appropriate response in the form of a JSON object.
 * <p>
 * Responsibilities:
 * - Retrieves the AI name from the {@link SystemSession} singleton.
 * - Constructs a generic response using {@link GenericResponse}.
 * - Returns the AI name in the format: "My name is [AI Name]".
 * <p>
 * Usage Scenario:
 * This handler is invoked whenever the user asks the system about its name. It utilizes
 * application-wide system states and response formatting utilities to generate consistent
 * outputs.
 * <p>
 * Exceptions:
 * - Throws {@link Exception} if there are any issues during the handling process.
 */
public class WhatIsYourNameHandler implements QueryHandler {

    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        SystemSession systemSession = SystemSession.getInstance();
        return GenericResponse.getInstance().genericResponse("My name is " + systemSession.getAIVoice().getName());
    }
}

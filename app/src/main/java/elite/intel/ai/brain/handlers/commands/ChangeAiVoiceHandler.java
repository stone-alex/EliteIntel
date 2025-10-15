package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;

/**
 * The SetAiVoice class implements the CommandHandler interface and facilitates the
 * handling of a command to set or change the AI voice in the system.
 * <p>
 * This class processes the parameters received through a command, validates the input,
 * and updates the system's AI voice configuration. If the provided voice name is invalid
 * or null, an error event is published via the EventBusManager.
 */
public class ChangeAiVoiceHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {
        String voiceName = params.get("key").getAsString();
        if (voiceName == null || voiceName.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("Sorry, the value returned was null or empty. I am unable to process your request."));
            return;
        }
        setVoice(voiceName);
    }

    private void setVoice(String voiceName) {
        SystemSession systemSession = SystemSession.getInstance();
        try {
            systemSession.setAIVoice(AiVoices.valueOf(voiceName.toUpperCase()));
            EventBusManager.publish(new AiVoxResponseEvent("AI voice set to " + voiceName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            EventBusManager.publish(new AiVoxResponseEvent("Sorry, I don't understand voice name: " + voiceName.toUpperCase() + ". Error: " + e.getMessage()));
        }
    }
}

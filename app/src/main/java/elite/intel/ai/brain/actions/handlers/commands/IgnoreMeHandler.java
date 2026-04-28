package elite.intel.ai.brain.actions.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.VoiceInputModeToggleEvent;

public class IgnoreMeHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        SystemSession.getInstance().stopStartListening(true);
        EventBusManager.publish(new VoiceInputModeToggleEvent(true));
    }
}

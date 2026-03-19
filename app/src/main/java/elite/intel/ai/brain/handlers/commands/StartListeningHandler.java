package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.VoiceInputModeToggleEvent;

public class StartListeningHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {
        SystemSession.getInstance().stopStartListening(false);
        EventBusManager.publish(new VoiceInputModeToggleEvent(false));
    }
}

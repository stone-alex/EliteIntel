package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.VoiceInputModeToggleEvent;

public class SetWakeWordOnOffHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {

        boolean isOn = params.get("state").getAsBoolean();
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.setStreamingMode(isOn);
        EventBusManager.publish(new VoiceInputModeToggleEvent(isOn));
    }
}

package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;

public class SetRadioTransmissionOnOff implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {

        boolean isOn = params.get("state").getAsBoolean();
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setRadioTransmissionOn(isOn);
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Radio is: " + (isOn ? "On" : "Off")));
    }
}

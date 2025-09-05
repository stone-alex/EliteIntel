package elite.companion.comms.handlers.command;

import com.google.gson.JsonObject;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.util.EventBusManager;

public class ClearSessionHandler implements CommandHandler {

    @Override
    public void handle(JsonObject params, String responseText) {
/*
        PlayerSession.getInstance().clearOnShutDown();
        SystemSession.getInstance().clearOnShutDown();
*/
        EventBusManager.publish(new VoiceProcessEvent("Session data cleared."));
    }
}
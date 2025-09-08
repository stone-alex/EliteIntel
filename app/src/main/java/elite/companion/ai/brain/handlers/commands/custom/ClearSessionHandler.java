package elite.companion.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.companion.ai.brain.handlers.commands.CommandHandler;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;

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
package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

public class ClearSessionHandler implements CommandHandler {

    @Override
    public void handle(JsonObject params, String responseText) {

        PlayerSession.getInstance().clearOnShutDown();

        SystemSession.getInstance().clearSystemConfigValues();
        SystemSession.getInstance().clearChatHistory();

        EventBusManager.publish(new VoiceProcessEvent("Session data cleared."));
    }
}
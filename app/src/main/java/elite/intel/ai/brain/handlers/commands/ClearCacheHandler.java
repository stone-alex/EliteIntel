package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

public class ClearCacheHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.clearCash();


        SystemSession systemSession = SystemSession.getInstance();
        systemSession.clearChatHistory();

        EventBusManager.publish(new AiVoxResponseEvent("Session data cleared."));
    }
}
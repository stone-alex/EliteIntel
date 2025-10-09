package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

public class ClearCacheHandler implements CommandHandler {

    @Override
    public void handle(String action, JsonObject params, String responseText) {

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.clearOnShutDown();
        playerSession.clearCash();


        SystemSession systemSession = SystemSession.getInstance();
        systemSession.clearSystemConfigValues();
        systemSession.clearChatHistory();

        EventBusManager.publish(new AiVoxResponseEvent("Session data cleared."));
    }
}
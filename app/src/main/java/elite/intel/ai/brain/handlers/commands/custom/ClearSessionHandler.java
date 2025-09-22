package elite.intel.ai.brain.handlers.commands.custom;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

public class ClearSessionHandler implements CommandHandler {

    @Override
    public void handle(JsonObject params, String responseText) {

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.clearOnShutDown();
        playerSession.setCurrentLocation(new LocationDto());

        SystemSession systemSession = SystemSession.getInstance();
        systemSession.clearSystemConfigValues();
        systemSession.clearChatHistory();

        EventBusManager.publish(new VoiceProcessEvent("Session data cleared."));
    }
}
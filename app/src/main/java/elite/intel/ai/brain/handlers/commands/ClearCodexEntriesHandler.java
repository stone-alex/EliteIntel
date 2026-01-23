package elite.intel.ai.brain.handlers.commands;


import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.CodexEntryManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class ClearCodexEntriesHandler implements CommandHandler {

    @Override public void handle(String action, JsonObject params, String responseText) {

        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        CodexEntryManager codexEntryManager = CodexEntryManager.getInstance();
        codexEntryManager.clear();
        playerSession.saveLocation(currentLocation);
        EventBusManager.publish(new AiVoxResponseEvent("Codex entries cleared."));
    }
}

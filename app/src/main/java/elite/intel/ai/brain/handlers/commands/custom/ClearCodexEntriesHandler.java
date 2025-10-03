package elite.intel.ai.brain.handlers.commands.custom;


import com.google.gson.JsonObject;
import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class ClearCodexEntriesHandler implements CommandHandler {

    @Override public void handle(JsonObject params, String responseText) {

        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.clearCodexEntries();
        playerSession.saveLocation(currentLocation);
        EventBusManager.publish(new AiVoxResponseEvent("Codex entries cleared."));
    }
}

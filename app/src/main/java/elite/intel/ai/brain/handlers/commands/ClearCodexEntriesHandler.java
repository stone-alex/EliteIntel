package elite.intel.ai.brain.handlers.commands;


import com.google.gson.JsonObject;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.CodexEntryManager;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class ClearCodexEntriesHandler implements CommandHandler {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();
    @Override public void handle(String action, JsonObject params, String responseText) {


        LocationDto currentLocation = locationManager.findByLocationData(playerSession.getLocationData());
        CodexEntryManager codexEntryManager = CodexEntryManager.getInstance();
        codexEntryManager.clear();
        locationManager.save(currentLocation);
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Codex entries cleared."));
    }
}

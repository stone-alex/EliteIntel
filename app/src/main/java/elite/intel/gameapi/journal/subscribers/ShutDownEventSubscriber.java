package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.ShutdownEvent;
import elite.intel.session.LocationHistory;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.SystemShutDownEvent;

public class ShutDownEventSubscriber {

    @Subscribe
    public void onShutDownEvent(ShutdownEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.save();
        LocationHistory history = LocationHistory.getInstance(playerSession.getCurrentLocation().getStarName());
        history.addLocations(playerSession.getLocations());

        EventBusManager.publish(new VoiceProcessEvent("Session off line..."));
        EventBusManager.publish(new SystemShutDownEvent());
    }
}

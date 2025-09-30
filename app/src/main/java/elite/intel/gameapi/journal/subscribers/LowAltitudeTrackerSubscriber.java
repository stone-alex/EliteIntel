package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.util.NavigationUtils;

public class LowAltitudeTrackerSubscriber {

    private long lastAnnounceTime = 0;

    @Subscribe
    public void onPlayerMoved(PlayerMovedEvent event) {
        long NOW = System.currentTimeMillis();
        NavigationUtils.Direction navigator = NavigationUtils.getDirections(0, 0, event);

        if (event.getAltitude() > 5 && event.getAltitude() < 1000 && navigator.getSpeed() > 200) {
            if (NOW - lastAnnounceTime > 5_000) {
                EventBusManager.publish(new VoiceProcessEvent("Altitude!. Pull Up!"));
                lastAnnounceTime = NOW;
            }
        }
    }
}

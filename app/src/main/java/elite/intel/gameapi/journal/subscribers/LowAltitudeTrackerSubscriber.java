package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.util.NavigationUtils;

public class LowAltitudeTrackerSubscriber {

    private long lastAnnounceTime = 0;
    private double lastAltitude = 0;

    @Subscribe
    public void onPlayerMoved(PlayerMovedEvent event) {
        long NOW = System.currentTimeMillis();
        NavigationUtils.Direction navigator = NavigationUtils.getDirections(0, 0, event);

        boolean flyingLow = event.getAltitude() > 5 && event.getAltitude() < 666;
        boolean flyingFast = navigator.getSpeed() > 200;
        boolean descending = lastAltitude < event.getAltitude();

        if (flyingLow && flyingFast && descending) {
            if (NOW - lastAnnounceTime > 7_000) {
                EventBusManager.publish(new VoiceProcessEvent("Altitude!. Pull Up!"));
                lastAnnounceTime = NOW;
            }
        }

        lastAltitude = event.getAltitude();
    }
}

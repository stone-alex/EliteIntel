package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

public class StatusEventSubscriber {

    @Subscribe
    public void onStatusChangedEvent(GameEvents.StatusEvent event) {
        Status status = Status.getInstance();
        GameEvents.StatusEvent oldStatus = status.getStatus();
        String legalStatusBeforeChange = oldStatus == null ? null : oldStatus.getLegalState();

        if (legalStatusBeforeChange != null) {
            String legalState = event.getLegalState();
            if (legalState != null && !legalStatusBeforeChange.equalsIgnoreCase(legalState)) {
                EventBusManager.publish(new VoiceProcessEvent("Legal status changed to: " + legalState + ". "));
            }
        }

        status.setStatus(event);
        status.setLastStatusChange(System.currentTimeMillis());
        EventBusManager.publish(new PlayerMovedEvent(event.getLatitude(), event.getLongitude(), event.getPlanetRadius(), event.getAltitude()));
    }
}

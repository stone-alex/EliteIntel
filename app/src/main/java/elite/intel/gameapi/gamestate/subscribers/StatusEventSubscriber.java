package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.gameapi.gamestate.status_events.BeingInterdictedEvent;
import elite.intel.gameapi.gamestate.status_events.InGlideEvent;
import elite.intel.gameapi.gamestate.status_events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.session.Status;

public class StatusEventSubscriber {

    private boolean lowFuelAnnounced = false;
    private boolean lowOxygenAnnounced = false;
    private boolean lowHealthAnnounced = false;

    @Subscribe
    public void onStatusChangedEvent(GameEvents.StatusEvent event) {
        Status status = Status.getInstance();
        GameEvents.StatusEvent oldStatus = status.getStatus();
        String legalStatusBeforeChange = oldStatus == null ? null : oldStatus.getLegalState();

        if (legalStatusBeforeChange != null) {
            String legalState = event.getLegalState();
            if (legalState != null && !legalStatusBeforeChange.equalsIgnoreCase(legalState)) {
                EventBusManager.publish(new MissionCriticalAnnouncementEvent("Legal status changed to: " + legalState + ". "));
            }
        }

        status.setStatus(event);

        EventBusManager.publish(new PlayerMovedEvent(event.getLatitude(), event.getLongitude(), event.getPlanetRadius(), event.getAltitude()));

        /// --------------------------------------------------------------------------------------
        //TODO: Can throw custom events. like BeingInterdictedEvent if(status.isBeingInterdicted()){ publish event...}


        EventBusManager.publish(new InGlideEvent(status.isGlideMode()));


        if (status.isBeingInterdicted()) {
            EventBusManager.publish(new BeingInterdictedEvent());
        }


        /// --------------------------------------------------------------------------------------
        /// Mission-critical alerts.
        if(status.isLowFuel() && !lowFuelAnnounced) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Low fuel warning!"));
            lowFuelAnnounced = true;
        }

        if(status.isLowOxygen() && !lowOxygenAnnounced) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Low oxygen warning!"));
            lowOxygenAnnounced = true;
        }

        if(status.isLowHealth() && !lowHealthAnnounced){
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Low health warning!"));
            lowHealthAnnounced = true;
        }
    }
}

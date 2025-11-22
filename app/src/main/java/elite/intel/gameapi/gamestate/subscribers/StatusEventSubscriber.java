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
        if(status.isLowFuel()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Low fuel warning!"));
        }

        if(status.isLowOxygen()) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Low oxygen warning!"));
        }

        if(status.isLowHealth()){
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Low health warning!"));
        }
    }
}

package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
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

        determineCurrentLocation(status);

        status.setStatus(event);
        status.setLastStatusChange(System.currentTimeMillis());

        EventBusManager.publish(new PlayerMovedEvent(event.getLatitude(), event.getLongitude(), event.getPlanetRadius(), event.getAltitude()));

        /// --------------------------------------------------------------------------------------
        //TODO: Can throw custom events. like BeingInterdictedEvent if(status.isBeingInterdicted()){ publish event...}

        if (status.isGlideMode()) {
            EventBusManager.publish(new InGlideEvent());
        }

        if (status.isBeingInterdicted()) {
            //Enable when we want to use it for something.
            //EventBusManager.publish(new BeingInterdictedEvent());
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

    private void determineCurrentLocation(Status status) {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();

        if (status.getStatus() != null && status.getStatus().getDestination() != null) {
            boolean notSameBodyId = currentLocation.getBodyId() != status.getStatus().getDestination().getBody();
            boolean bodyNameDoesNotMatch = !currentLocation.getPlanetName().equalsIgnoreCase(status.getStatus().getDestination().getName());
            if (notSameBodyId && bodyNameDoesNotMatch) {
                playerSession.setCurrentLocationId(status.getStatus().getDestination().getBody());
            }
        }
    }
}

package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
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
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();

        if (status.getStatus() != null && status.getStatus().getDestination() != null) {
            boolean notSameBody = currentLocation.getBodyId() != status.getStatus().getDestination().getBody();
            boolean bodyNameDoesNotMatch = !currentLocation.getPlanetName().equalsIgnoreCase(status.getStatus().getDestination().getName());
            if (notSameBody && bodyNameDoesNotMatch) {
                playerSession.setCurrentLocationId(status.getStatus().getDestination().getBody());
            }
        }

        status.setStatus(event);
        status.setLastStatusChange(System.currentTimeMillis());
        EventBusManager.publish(new PlayerMovedEvent(event.getLatitude(), event.getLongitude(), event.getPlanetRadius(), event.getAltitude()));
    }
}

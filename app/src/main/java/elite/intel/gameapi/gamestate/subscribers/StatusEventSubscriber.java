package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.session.PlayerSession;

public class StatusEventSubscriber {

    @Subscribe
    public void onStatusChangedEvent(GameEvents.StatusEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        GameEvents.StatusEvent oldStatus = playerSession.getStatus();
        String legalStatusBeforeChange = oldStatus.getLegalState();

        if (legalStatusBeforeChange != null && !legalStatusBeforeChange.equalsIgnoreCase(event.getLegalState())) {
            EventBusManager.publish(new VoiceProcessEvent("Legal status changed to: " + event.getLegalState() + ". "));
        }

        playerSession.setStatus(event);
        EventBusManager.publish(new PlayerMovedEvent(event.getLatitude(), event.getLongitude(), event.getPlanetRadius(), event.getAltitude()));
    }
}

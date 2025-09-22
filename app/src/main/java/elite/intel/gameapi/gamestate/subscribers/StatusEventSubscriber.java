package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.gameapi.gamestate.events.PlayerMovedEvent;
import elite.intel.gameapi.journal.events.dto.StatusDto;
import elite.intel.session.PlayerSession;

public class StatusEventSubscriber {

    @Subscribe
    public void onStatusChangedEvent(GameEvents.StatusEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        StatusDto status = playerSession.getStatus() == null ? new StatusDto() : playerSession.getStatus();
        String legalStatusBeforeChange = status.getLegalState();
        if (legalStatusBeforeChange != null && !legalStatusBeforeChange.equalsIgnoreCase(event.getLegalState())) {
            EventBusManager.publish(new VoiceProcessEvent("Legal status changed to: " + event.getLegalState() + ". "));
        }


        status.setAltitude(event.getAltitude());
        status.setBalance(event.getBalance());
        status.setCargo(event.getCargo());
        status.setGuiFocus(event.getGuiFocus());
        status.setHeading(event.getHeading());
        status.setLatitude(event.getLatitude());
        status.setLongitude(event.getLongitude());
        status.setLegalState(event.getLegalState());
        status.setPips(event.getPips());
        status.setPlanetRadius(event.getPlanetRadius());
        playerSession.setStatus(status);


        if (!playerSession.getBioSamples().isEmpty()) {
            EventBusManager.publish(new PlayerMovedEvent(event.getLatitude(), event.getLongitude(), event.getPlanetRadius()));
        }
    }
}

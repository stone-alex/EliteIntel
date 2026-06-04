package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.RadioTransmissionEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.DockingGrantedEvent;
import elite.intel.session.PlayerSession;

public class DockingRequestGrantedSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    @Subscribe
    public void onDockingRequestGrantedEvent(DockingGrantedEvent event) {
        String playerName = playerSession.getVariablePlayerName();

        if (event.getStationName().equalsIgnoreCase(playerSession.getFleetCarrierData().getCallSign())) {
            String personalGreeting = " Welcome home " + playerName + "!";
            EventBusManager.publish(
                    new RadioTransmissionEvent(
                            "This is %s traffic control. Proceed to landing pad %d, %s"
                                    .formatted(
                                            playerSession.getFleetCarrierData().getCarrierName(),
                                            event.getLandingPad(),
                                            personalGreeting
                                    )
                    )
            );
        } else {
            String personalGreeting = " Good to see you " + playerName + "!";
            EventBusManager.publish(
                    new RadioTransmissionEvent(
                            "This is %s traffic control. Proceed to landing pad %d, %s"
                                    .formatted(
                                            event.getStationName(),
                                            event.getLandingPad(),
                                            personalGreeting
                                    )
                    )
            );
        }
    }
}

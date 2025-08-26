package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.ShipTargetedEvent;

public class ShipTargetedEventSubscriber {

    public ShipTargetedEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onShipTargetedEvent(ShipTargetedEvent event) {
        String pilotName = event.getPilotNameLocalised();
        String pilotRank = event.getPilotRank();
        String ship = event.getShip();
        String legalStatus = event.getLegalStatus();

        StringBuilder info = new StringBuilder();
        info.append("Scanning:");
        info.append(" ");
        info.append(pilotName);
        info.append(" ");
        info.append(pilotRank);
        info.append(" ");
        info.append(ship);
        info.append(" ");
        info.append(legalStatus);

        VoiceGenerator.getInstance().speak(info.toString());
    }
}

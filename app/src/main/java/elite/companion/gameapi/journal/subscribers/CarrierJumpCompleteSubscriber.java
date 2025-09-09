package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.CarrierJumpEvent;
import elite.companion.session.PlayerSession;

@SuppressWarnings("unused")
public class CarrierJumpCompleteSubscriber {

    @Subscribe
    public void onCarrierJumpCompleteEvent(CarrierJumpEvent event) {
        String starSystem = event.getStarSystem();
        PlayerSession.getInstance().put(PlayerSession.CARRIER_LOCATION, "StarSystem: " + starSystem + ", planet " + event.getBody());
        EventBusManager.publish(new SensorDataEvent("Carrier Location: " + event.toJson()));
    }
}



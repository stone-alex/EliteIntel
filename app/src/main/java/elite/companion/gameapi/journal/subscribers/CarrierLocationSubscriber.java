package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.CarrierLocationEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

public class CarrierLocationSubscriber {

    @Subscribe
    public void onCarrierLocationEvent(CarrierLocationEvent event) {

        if ("FleetCarrier".equalsIgnoreCase(event.getCarrierType())) {
            SystemSession.getInstance().put(PlayerSession.CARRIER_LOCATION, event.getStarSystem());
            EventBusManager.publish(new SensorDataEvent("FleetCarrier jump complete, new location: " + event.getStarSystem()));
        }
    }
}

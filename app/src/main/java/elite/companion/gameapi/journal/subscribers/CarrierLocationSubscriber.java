package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.CarrierLocationEvent;
import elite.companion.session.SystemSession;

public class CarrierLocationSubscriber {

    @Subscribe
    public void onCarrierLocationEvent(CarrierLocationEvent event) {

        if("fleetcarrier".equalsIgnoreCase(event.getCarrierType())) {
            //Fleet Carrier Location Event
            SystemSession.getInstance().put(SystemSession.CARRIER_LOCATION, event.getStarSystem());
        }
    }
}

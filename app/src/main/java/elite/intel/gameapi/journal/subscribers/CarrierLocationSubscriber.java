package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.CarrierLocationEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.session.PlayerSession;

public class CarrierLocationSubscriber {

    @Subscribe
    public void onCarrierLocationEvent(CarrierLocationEvent event) {

        if ("FleetCarrier".equalsIgnoreCase(event.getCarrierType())) {
            PlayerSession playerSession = PlayerSession.getInstance();
            playerSession.setLastKnownCarrierLocation(event.getStarSystem());
            playerSession.save();
        }
    }
}

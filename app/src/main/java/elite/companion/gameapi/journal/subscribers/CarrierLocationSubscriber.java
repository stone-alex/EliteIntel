package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.CarrierLocationEvent;
import elite.companion.gameapi.journal.events.dto.CarrierDataDto;
import elite.companion.session.PlayerSession;

public class CarrierLocationSubscriber {

    @Subscribe
    public void onCarrierLocationEvent(CarrierLocationEvent event) {

        if ("FleetCarrier".equalsIgnoreCase(event.getCarrierType())) {
            PlayerSession playerSession = PlayerSession.getInstance();
            CarrierDataDto carrierData = playerSession.getCarrierData();
            if(carrierData != null) {
                carrierData.setLocation(event.getStarSystem());
                playerSession.setCarrierData(carrierData);
            }
            EventBusManager.publish(new SensorDataEvent("FleetCarrier location: " + event.getStarSystem()));
        }
    }
}

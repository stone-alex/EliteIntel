package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.search.edsm.EdsmApiClient;
import elite.intel.ai.search.edsm.dto.DeathsDto;
import elite.intel.ai.search.edsm.dto.TrafficDto;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.CarrierJumpEvent;
import elite.intel.gameapi.journal.events.dto.CarrierDataDto;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class CarrierJumpCompleteSubscriber {

    @Subscribe
    public void onCarrierJumpCompleteEvent(CarrierJumpEvent event) {
        String starSystem = event.getStarSystem();
        double[] starPos = event.getStarPos();
        PlayerSession playerSession = PlayerSession.getInstance();
        CarrierDataDto carrierData = playerSession.getCarrierData();
        playerSession.remove(PlayerSession.CARRIER_DEPARTURE_TIME);

        if(carrierData != null) {
            carrierData.setX(starPos[0]);
            carrierData.setY(starPos[1]);
            carrierData.setZ(starPos[2]);
            carrierData.setLocation(starSystem);
            playerSession.setCarrierData(carrierData);
        }

        EventBusManager.publish(new SensorDataEvent("Carrier Location: " + event.getStarSystem()));
    }
}
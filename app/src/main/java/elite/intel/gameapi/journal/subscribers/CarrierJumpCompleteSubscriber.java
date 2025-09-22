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

        if(carrierData != null) {
            carrierData.setX(starPos[0]);
            carrierData.setY(starPos[1]);
            carrierData.setZ(starPos[2]);
            carrierData.setLocation(starSystem);
            playerSession.setCarrierData(carrierData);
        }



        if(event.isOnFoot()){
            // we are on the carrier during the jump.
            LocationDto currentLocation = playerSession.getCurrentLocation();
            currentLocation.setStarName(starSystem);
            currentLocation.setX(starPos[0]);
            currentLocation.setY(starPos[1]);
            currentLocation.setZ(starPos[2]);

            currentLocation.setPopulation(event.getPopulation());
            currentLocation.setPlanetName(event.getBody());
            currentLocation.setAllegiance(event.getSystemAllegiance());
            currentLocation.setSecurity(event.getSystemSecurityLocalised());
            currentLocation.setPowerplayState(event.getPowerplayState());
            currentLocation.setPowerplayStateControlProgress(event.getPowerplayStateControlProgress());
            currentLocation.setPowerplayStateReinforcement(event.getPowerplayStateReinforcement());
            currentLocation.setPowerplayStateUndermining(event.getPowerplayStateUndermining());

            currentLocation.clearSaaSignals();
            currentLocation.clearDetectedSignals();
            currentLocation.clearGenus();
            currentLocation.setPlanetData(null);

            DeathsDto deathsDto = EdsmApiClient.searchDeaths(starSystem);
            if (deathsDto.getData() != null && deathsDto.getData().getDeaths() != null) {
                currentLocation.setDeathsDto(deathsDto);
            }
            TrafficDto trafficDto = EdsmApiClient.searchTraffic(starSystem);
            if (trafficDto.getData() != null && trafficDto.getData().getTraffic() != null) {
                currentLocation.setTrafficDto(trafficDto);
            }

            playerSession.setCurrentLocation(currentLocation);
        }

        EventBusManager.publish(new SensorDataEvent("Carrier Location: " + event.toJson()));
    }
}



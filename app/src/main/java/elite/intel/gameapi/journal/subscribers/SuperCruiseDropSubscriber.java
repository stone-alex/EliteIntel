package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.SupercruiseDestinationDropEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.StringUtls;

public class SuperCruiseDropSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Subscribe
    public void onSuperCruiseDrop(SupercruiseDestinationDropEvent event) {

        if (event.getThreat() > 0) {
            EventBusManager.publish(new SensorDataEvent(" Dropped from supercruise. Threat level: " + event.getThreat() + ". ", "Notify user about supercruise exit and threat level"));
            return;
        }

        LocationDto location = locationManager.findByMarketId(event.getMarketID());
        if(location.getBodyId() < 1 && location.getSystemAddress() < 1){
            location.setLocationType(LocationDto.LocationType.STATION);
            location.setSystemAddress(playerSession.getLocationData().getSystemAddress());
            locationManager.save(location);
        }

        String carrierName = playerSession.getCarrierData().getCarrierName();
        if (event.getType().toUpperCase().startsWith(carrierName.toUpperCase())) {
            EventBusManager.publish(new SensorDataEvent("Welcome Home to " + StringUtls.capitalizeWords(carrierName) + "! ", "We are back at base"));
        }
    }
}

package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.SupercruiseEntryEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class SupercruiseEntryEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(SupercruiseEntryEventSubscriber.class);

    @Subscribe
    public void onSuperCruiseEntryEvent(SupercruiseEntryEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.setStationFaction(null);
        currentLocation.setStationName(null);
        currentLocation.setStationServices(new ArrayList<>());
        currentLocation.setStationAllegiance(null);
        currentLocation.setStationEconomy(null);
        currentLocation.setStationGovernment(null);
        currentLocation.setStationType(null);
        playerSession.setCurrentLocation(currentLocation);

        LocalServicesData.clearLocalServicesData();
    }
}

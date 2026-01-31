package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.journal.events.ScanBaryCentreEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class ScanBaryCentreSubscriber {

    private final LocationManager locationManager = LocationManager.getInstance();

    @Subscribe
    public void onScanBaryCentreEvent(ScanBaryCentreEvent event) {
        LocationDto location = locationManager.findBySystemAddress(event.getSystemAddress(), event.getBodyID());
        LocationDto primaryStarLocation = locationManager.findPrimaryStar(event.getStarSystem());
        location.setBodyId(event.getBodyID());
        location.setStarName(primaryStarLocation.getStarName());
        location.setX(primaryStarLocation.getX());
        location.setY(primaryStarLocation.getY());
        location.setZ(primaryStarLocation.getZ());
        location.setStarName(event.getStarSystem());
        location.setBodyId(event.getBodyID());
        location.setSystemAddress(event.getSystemAddress());
        location.setOrbitalPeriod(event.getOrbitalPeriod());
        locationManager.save(location);
    }
}

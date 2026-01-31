package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.journal.events.SupercruiseExitEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.core.appender.FailoverAppender;

@SuppressWarnings("unused")
public class SupercruiseExitedSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final LocationManager locationManager = LocationManager.getInstance();

    @Subscribe
    public void onSupercruiseExited(SupercruiseExitEvent event) {
        LocationDto starSystem = locationManager.findPrimaryStar(playerSession.getPrimaryStarName());
        LocationDto here = locationManager.findBySystemAddress(event.getSystemAddress(), event.getBodyId());

        here.setBodyType(event.getBodyType());
        here.setBodyId(event.getBodyId());
        here.setSystemAddress(event.getSystemAddress());
        here.setStarName(playerSession.getPrimaryStarName());
        here.setLandingCoordinates(starSystem.getLandingCoordinates());

        LocationDto.LocationType locationType = LocationDto.determineType(event.getBodyType(), false);
        if(LocationDto.LocationType.STATION == locationType){
            here.setStationName(event.getBody());
        } else if(LocationDto.LocationType.PLANET == locationType || LocationDto.LocationType.MOON == locationType ) {
            here.setPlanetName(event.getBody());
        }

        if (here.getLocationType() == null || LocationDto.LocationType.UNKNOWN == here.getLocationType()) {
            here.setLocationType(locationType);
        }

        playerSession.setCurrentLocationId(event.getBodyId(), event.getSystemAddress());
    }

}

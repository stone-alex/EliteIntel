package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.SupercruiseExitEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class SupercruiseExitedSubscriber {

    @Subscribe
    public void onSupercruiseExited(SupercruiseExitEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();

        LocationDto currentLocation = playerSession.getCurrentLocation();
        currentLocation.setStarName(event.getStarSystem());
        currentLocation.setBodyId(event.getBodyId());
        currentLocation.setPlanetName(event.getBody());
        playerSession.saveLocation(currentLocation);
    }

}

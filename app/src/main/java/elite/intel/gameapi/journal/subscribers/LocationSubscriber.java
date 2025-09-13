package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.LocationEvent;
import elite.intel.gameapi.journal.events.dto.LocationDto;
import elite.intel.session.PlayerSession;

public class LocationSubscriber {

    @Subscribe
    public void onLocationEvent(LocationEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.put(PlayerSession.CURRENT_SYSTEM_NAME, event.getStarSystem());

        LocationDto dto = new LocationDto();
        dto.setX(event.getStarPos()[0]);
        dto.setY(event.getStarPos()[1]);
        dto.setZ(event.getStarPos()[2]);
        dto.setStarName(event.getStarSystem());
        playerSession.setCurrentLocation(dto);
        playerSession.addSignal(dto);
    }
}

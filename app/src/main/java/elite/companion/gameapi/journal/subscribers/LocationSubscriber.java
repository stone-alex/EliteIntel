package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.LocationEvent;
import elite.companion.session.PlayerSession;

public class LocationSubscriber {

    @Subscribe
    public void onLocationEvent(LocationEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.put(PlayerSession.CURRENT_LOCATION, event.toJson());
        playerSession.put(PlayerSession.CURRENT_SYSTEM_NAME, event.getStarSystem());
    }
}

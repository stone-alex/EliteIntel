package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.LocationEvent;
import elite.companion.session.SystemSession;

public class LocationSubscriber {

    @Subscribe
    public void onLocationEvent(LocationEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.put(SystemSession.CURRENT_LOCATION, event.toJson());
        systemSession.put(SystemSession.CURRENT_SYSTEM, event.getStarSystem());
    }
}

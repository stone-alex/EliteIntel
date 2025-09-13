package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.NavRouteClearEvent;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class RoutClearedSubscriber {

    @Subscribe
    public void onRouteCleared(NavRouteClearEvent event) {
        PlayerSession.getInstance().clearRoute();
    }

}

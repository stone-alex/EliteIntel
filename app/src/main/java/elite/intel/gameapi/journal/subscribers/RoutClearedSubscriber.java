package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.NavRouteClearEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.ShipRoute;

@SuppressWarnings("unused")
public class RoutClearedSubscriber {

    @Subscribe
    public void onRouteCleared(NavRouteClearEvent event) {
        ShipRoute.getInstance().clearRoute();
    }

}

package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.NavRouteEvent;

public class NavRouteSetSubscriber {

    @Subscribe
    public void onNavRouteSetEvent(NavRouteEvent event) {
        EventBusManager.publish(new VocalisationRequestEvent("Route set"));
    }
}

package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.handlers.commands.custom.NavigationOnOffHandler;
import elite.intel.ai.mouth.subscribers.events.NavigationVocalisationEvent;
import elite.intel.ai.mouth.subscribers.events.RouteAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.NavRouteEvent;

public class NavRouteSetSubscriber {

    @Subscribe
    public void onNavRouteSetEvent(NavRouteEvent event) {
        EventBusManager.publish(new RouteAnnouncementEvent("Route set"));
    }
}

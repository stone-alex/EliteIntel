package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.NavRouteEvent;
import elite.companion.util.EventBusManager;

public class NavRouteSetSubscriber {

    @Subscribe
    public void onNavRouteSetEvent(NavRouteEvent event) {
        EventBusManager.publish(new VoiceProcessEvent("Route set"));
    }
}

package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.LiftoffEvent;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class LiftoffEventSubscriber {

    @Subscribe
    public void onLiftoffEvent(LiftoffEvent event) {
        // if we have a trade route.
        // and if the market name matches the current location.
        // remove the leg from the trade route.
    }
}

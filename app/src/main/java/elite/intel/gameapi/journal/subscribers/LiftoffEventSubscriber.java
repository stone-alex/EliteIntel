package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.LiftoffEvent;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class LiftoffEventSubscriber {

    @Subscribe
    public void onLiftoffEvent(LiftoffEvent event) {
        // not sure what to do with this event yet.
    }
}

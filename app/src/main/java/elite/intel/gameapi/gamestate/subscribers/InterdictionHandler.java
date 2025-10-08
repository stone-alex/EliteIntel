package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.gamestate.status_events.BeingInterdictedEvent;

public class InterdictionHandler {


    @Subscribe
    public void onInterdictedEvent(BeingInterdictedEvent event) {
        // not sure what we can legally do here yet.
    }
}

package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.SupercruiseExitEvent;

@SuppressWarnings("unused")
public class SupercruiseExitedSubscriber {

    @Subscribe
    public void onSupercruiseExited(SupercruiseExitEvent event) {
        //TODO: something....
    }

}

package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.DockedEvent;

public class DockSRVEventSubscriber {

    @Subscribe
    public void onDockSRVEvent(DockedEvent event) {
        //implement docked event
    }
}

package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.DockedEvent;

public class DockSRVEventSubscriber {

    @Subscribe
    public void onDockSRVEvent(DockedEvent event) {
        //implement docked event
    }
}

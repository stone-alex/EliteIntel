package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.DockedEvent;
import elite.intel.session.PlayerSession;

public class DockSRVEventSubscriber {

    @Subscribe
    public void onDockSRVEvent(DockedEvent event) {
        EventBusManager.publish(new AiVoxResponseEvent("Welcome back aboard, " + PlayerSession.getInstance().randomPlayerName()));
    }
}

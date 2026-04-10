package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.DockSRVEvent;
import elite.intel.session.PlayerSession;

public class DockSRVEventSubscriber {

    @Subscribe
    public void onDockSRVEvent(DockSRVEvent event) {
        EventBusManager.publish(new AiVoxResponseEvent("Welcome back aboard, " + PlayerSession.getInstance().randomPlayerName()));
    }
}

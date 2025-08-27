package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.FSSSignalDiscoveredEvent;
import elite.companion.session.SystemSession;

public class FSSSignalDiscoveredSubscriber {
    private final SystemSession systemSession = SystemSession.getInstance();

    public FSSSignalDiscoveredSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onFSSSignalDiscovered(FSSSignalDiscoveredEvent event) {
        systemSession.addSignal(event);
    }
}

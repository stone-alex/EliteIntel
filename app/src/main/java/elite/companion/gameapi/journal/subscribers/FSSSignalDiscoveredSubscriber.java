package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.FSSSignalDiscoveredEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class FSSSignalDiscoveredSubscriber {


    @Subscribe
    public void onFSSSignalDiscovered(FSSSignalDiscoveredEvent event) {
        PlayerSession.getInstance().addSignal(event);
    }
}

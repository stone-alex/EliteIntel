package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;
import elite.intel.session.PlayerSession;

public class FSSBodySignalsSubscriber {

    @Subscribe
    public void onFssBodySignal(FSSBodySignalsEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.putFssBodySignal(event.getBodyID(), event);
    }
}

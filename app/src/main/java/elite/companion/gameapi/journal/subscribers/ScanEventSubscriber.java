package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.ScanEvent;
import elite.companion.session.PlayerSession;

@SuppressWarnings("unused")
public class ScanEventSubscriber {

    @Subscribe
    public void onScanEvent(ScanEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.put(PlayerSession.LAST_SCAN, event.toJson());
    }
}

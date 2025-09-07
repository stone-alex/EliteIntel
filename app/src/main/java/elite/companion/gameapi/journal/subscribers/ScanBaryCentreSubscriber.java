package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.ScanBaryCentreEvent;

public class ScanBaryCentreSubscriber {

    @Subscribe
    public void onScanBaryCentreEvent(ScanBaryCentreEvent event) {
        //implement scan bary centre event
    }
}

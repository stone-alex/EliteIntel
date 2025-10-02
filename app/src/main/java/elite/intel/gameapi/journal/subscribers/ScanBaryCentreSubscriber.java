package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.ScanBaryCentreEvent;

public class ScanBaryCentreSubscriber {

    @Subscribe
    public void onScanBaryCentreEvent(ScanBaryCentreEvent event) {
        System.out.println("ScanBaryCentreEvent not implemented yet.");
    }
}

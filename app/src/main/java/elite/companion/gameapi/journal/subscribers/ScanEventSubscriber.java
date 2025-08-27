package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.ScanEvent;
import elite.companion.session.SystemSession;

public class ScanEventSubscriber {


    public ScanEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onScanEvent(ScanEvent event) {
        SystemSession.getInstance().setSensorData(event.toString());
    }
}

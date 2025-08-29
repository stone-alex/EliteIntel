package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.ScanEvent;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class BodyScanEventSubscriber {

    @Subscribe
    public void onScanEvent(ScanEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        boolean announce = Boolean.TRUE.equals(systemSession.getObject(SystemSession.ANNOUNCE_BODY_SCANS));
        if (announce) {
            systemSession.addBodySignal(event);
        }
    }
}

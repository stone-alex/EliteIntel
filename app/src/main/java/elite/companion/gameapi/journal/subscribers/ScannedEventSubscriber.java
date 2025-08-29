package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.ScannedEvent;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class ScannedEventSubscriber {

    @Subscribe
    public void onScannedEvent(ScannedEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.addSignal(event);

        if ("cargo".equals(event.getScanType().toLowerCase())) {
            systemSession.sendToAiAnalysis("Pirate scan detected.");
        }
    }
}

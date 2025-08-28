package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.ScannedEvent;
import elite.companion.session.SystemSession;

public class ScannedEventSubscriber {

    public ScannedEventSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onScannedEvent(ScannedEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.addSignal(event);

        if ("cargo".equals(event.getScanType().toLowerCase())) {
            systemSession.setSensorData("Pirate scan detected.");
        }
    }
}

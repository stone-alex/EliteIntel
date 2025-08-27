package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.ScannedEvent;
import elite.companion.session.SystemSession;

public class ScannedEventSubscriber {

    public ScannedEventSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onScannedEvent(ScannedEvent event) {
        SystemSession systemSession = SystemSession.getInstance();

        if ("cargo".equals(event.getScanType().toLowerCase())) {
            systemSession.setSensorData("Pirate scan detected. Issue a warning. Opportunity to collect bounty");
        } else {
            systemSession.setSensorData("Notify pilot that we are being scanned");
        }
    }
}

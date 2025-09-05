package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.ScannedEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

@SuppressWarnings("unused")
public class ScannedEventSubscriber {

    @Subscribe
    public void onScannedEvent(ScannedEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        if ("cargo".equalsIgnoreCase(event.getScanType())) {
            EventBusManager.publish(new VoiceProcessEvent("Cargo scan detected!"));
        }
    }
}

package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.ScannedEvent;
import elite.intel.session.SystemSession;

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

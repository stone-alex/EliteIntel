package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.ShutdownEvent;

public class ShutDownEventSubscriber {

    @Subscribe
    public void onShutDownEvent(ShutdownEvent event) {
        EventBusManager.publish(new VoiceProcessEvent("Session off line..."));
        //PlayerSession.getInstance().clearOnShutDown();
        //SystemSession.getInstance().clearOnShutDown();
    }
}

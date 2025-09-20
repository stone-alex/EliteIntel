package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.gameapi.journal.events.ShutdownEvent;
import elite.intel.ui.event.SystemShutDownEvent;

public class ShutDownEventSubscriber {

    @Subscribe
    public void onShutDownEvent(ShutdownEvent event) {
        EventBusManager.publish(new VoiceProcessEvent("Session off line..."));
        EventBusManager.publish(new SystemShutDownEvent());
        //PlayerSession.getInstance().clearOnShutDown();
        //SystemSession.getInstance().clearOnShutDown();
    }
}

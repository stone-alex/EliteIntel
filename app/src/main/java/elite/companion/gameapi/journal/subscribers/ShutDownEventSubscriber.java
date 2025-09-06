package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.ShutdownEvent;

public class ShutDownEventSubscriber {

    @Subscribe
    public void onShutDownEvent(ShutdownEvent event) {
        EventBusManager.publish(new VoiceProcessEvent("Session off line..."));
        //PlayerSession.getInstance().clearOnShutDown();
        //SystemSession.getInstance().clearOnShutDown();
    }
}

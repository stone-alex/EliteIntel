package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.ShutdownEvent;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.SystemShutDownEvent;

public class ShutDownEventSubscriber {

    @Subscribe
    public void onShutDownEvent(ShutdownEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.save();
        EventBusManager.publish(new VocalisationRequestEvent("Session off line..."));
        EventBusManager.publish(new SystemShutDownEvent());
    }
}

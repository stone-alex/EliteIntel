package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ai.mouth.subscribers.events.VocalisationRequestEvent;
import elite.intel.gameapi.journal.events.ShutdownEvent;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.SystemShutDownEvent;

public class ShutDownEventSubscriber {

    @Subscribe
    public void onShutDownEvent(ShutdownEvent event) {
        EventBusManager.publish(new SystemShutDownEvent());
    }
}

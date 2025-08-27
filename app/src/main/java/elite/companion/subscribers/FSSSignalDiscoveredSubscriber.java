package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.FSSSignalDiscoveredEvent;
import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FSSSignalDiscoveredSubscriber {
    private final SystemSession systemSession = SystemSession.getInstance();

    public FSSSignalDiscoveredSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onFSSSignalDiscovered(FSSSignalDiscoveredEvent event) {
        systemSession.addSignal(event);
    }
}

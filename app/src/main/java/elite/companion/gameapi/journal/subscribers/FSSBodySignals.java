package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.FSSBodySignalsEvent;
import elite.companion.util.EventBusManager;

public class FSSBodySignals {

    @Subscribe
    public void onFSSBodySignals(FSSBodySignalsEvent event) {

        EventBusManager.publish(new SensorDataEvent(event.toJson()));

    }
}

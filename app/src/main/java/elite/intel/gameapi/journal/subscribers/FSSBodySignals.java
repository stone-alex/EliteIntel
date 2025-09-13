package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;

public class FSSBodySignals {

    @Subscribe
    public void onFSSBodySignals(FSSBodySignalsEvent event) {

        EventBusManager.publish(new SensorDataEvent(event.toJson()));

    }
}

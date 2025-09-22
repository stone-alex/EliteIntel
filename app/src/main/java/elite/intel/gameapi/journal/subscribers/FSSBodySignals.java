package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.FSSBodySignalsEvent;

import java.util.List;

public class FSSBodySignals {

    @Subscribe
    public void onFSSBodySignals(FSSBodySignalsEvent event) {

        boolean containsLife = false;
        List<FSSBodySignalsEvent.Signal> signals = event.getSignals();
        for (FSSBodySignalsEvent.Signal signal : signals) {
            if ("Biological".equalsIgnoreCase(signal.getTypeLocalised())) {
                containsLife = true;
                break;
            }
        }

        if (containsLife) {
            EventBusManager.publish(new SensorDataEvent(event.toJson()));
        }
    }
}

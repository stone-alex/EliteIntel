package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.SellOrganicDataEvent;

public class SellOrganicDataSubscriber {

    @Subscribe
    public void onSellOrganicDataEvent(SellOrganicDataEvent event) {
        // not sure what to do with this yet.
        EventBusManager.publish(new SensorDataEvent(event.toJson()));
    }
}

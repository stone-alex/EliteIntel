package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.ShipyardBuyEvent;
import elite.intel.ui.event.ShipProfileChangedEvent;

public class NewShipPurchasedHandler {

    @Subscribe
    public void onNewShipPurchased(ShipyardBuyEvent event) {
        EventBusManager.publish(new SensorDataEvent("New ship added to fleet. Class: " + event.getShipType(), "Congratulate User on new addition to the fleet."));
        EventBusManager.publish(new ShipProfileChangedEvent());
    }

}

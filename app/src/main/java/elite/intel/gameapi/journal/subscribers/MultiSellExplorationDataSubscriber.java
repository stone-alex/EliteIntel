package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.MultiSellExplorationDataEvent;

public class MultiSellExplorationDataSubscriber {

    @Subscribe
    public void onMultiSellExplorationData(MultiSellExplorationDataEvent event) {
        String bonus = " Bonus credits: " + event.getBonus() + ". ";
        String totalSale = " Total Sale: " + event.getTotalEarnings() + " credits. ";
        int discoveredStarSystems = event.getDiscovered().size();

        EventBusManager.publish(new SensorDataEvent(" Sold: " + discoveredStarSystems + " star systems. Reward: " + bonus + totalSale));
    }
}

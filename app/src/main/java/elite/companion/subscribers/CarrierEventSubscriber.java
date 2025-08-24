package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.CarrierStatsEvent;
import elite.companion.session.PlayerStats;
import elite.companion.session.PublicSession;

public class CarrierEventSubscriber {

    public CarrierEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onEvent(CarrierStatsEvent event) {
        long carrierBalance = event.getFinance().getCarrierBalance();
        long reserveBalance = event.getFinance().getReserveBalance();

        PlayerStats playerStats = PublicSession.getInstance().getPlayerStats();
        playerStats.setCarrierBalance(String.valueOf(carrierBalance));
        playerStats.setCarrierReserve(String.valueOf(reserveBalance));
        EventBusManager.publish(playerStats);

        long carrierBalanceBillions = (carrierBalance / 100_000_000) * 100_000_000;
        long reserveBalanceBillions = (reserveBalance / 100_000_000) * 100_000_000;


        if (!EventTracker.isProcessed(event.getEventName())) {
            int jumps = event.getFuelLevel() / 90;
            PublicSession.getInstance().updateSession("carrier_stats", "Credit balance: " + carrierBalanceBillions + " reserved balance: " + reserveBalanceBillions + " fuel level: " + event.getFuelLevel() + " enough for " + event.getFuelLevel() / 90 + " jumps or " + (jumps * 500) + " light years");
        }
        EventTracker.setProcessed(event.getEventName());
    }
}

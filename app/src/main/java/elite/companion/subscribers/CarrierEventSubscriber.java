package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceNotifier;
import elite.companion.events.CarrierStatsEvent;
import elite.companion.session.PlayerStats;
import elite.companion.session.SessionTracker;

public class CarrierEventSubscriber {

    public CarrierEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onEvent(CarrierStatsEvent event) {
        String name = event.getName();
        long carrierBalance = event.getFinance().getCarrierBalance();
        long reserveBalance = event.getFinance().getReserveBalance();

        PlayerStats playerStats = SessionTracker.getPlayerStats();
        playerStats.setCarrierBalance(String.valueOf(carrierBalance));
        playerStats.setCarrierReserve(String.valueOf(reserveBalance));
        SessionTracker.setPlayerStats(playerStats);

        long carrierBalanceBillions = (carrierBalance / 100_000_000) * 100_000_000;
        long reserveBalanceBillions = (reserveBalance / 100_000_000) * 100_000_000;


        VoiceNotifier voiceNotifier = new VoiceNotifier();
        if (!EventTracker.isProcessed(event.getEventName())) {
            int jumps = event.getFuelLevel() / 90;

            voiceNotifier.speak(String.format("Welcome to %s, %s. We have around %d credits available. From that amount around %d credits are reserved for operations.", name, playerStats.getPlayerName(), carrierBalanceBillions, reserveBalanceBillions));
            voiceNotifier.speak(String.format("Our fuel level is %dTons. It should be enough for about %d jumps which is about %d light years.", event.getFuelLevel(), jumps, jumps * 500));
        }
        EventTracker.setProcessed(event.getEventName());
    }
}

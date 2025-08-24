package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.PowerplayEvent;
import elite.companion.session.PlayerStats;
import elite.companion.session.PublicSession;

public class PowerPlaySubscriber {

    public PowerPlaySubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onPowerPlayEvent(PowerplayEvent event) {
        PlayerStats playerStats = PublicSession.getInstance().getPlayerStats();
        playerStats.setPowerplayEvent(event);
        PublicSession.getInstance().updateSession("player_power_stats", "Pledged to " + event.getPower() + " at rank " + event.getRank() + " with " + event.getMerits() + " merits ");
    }
}

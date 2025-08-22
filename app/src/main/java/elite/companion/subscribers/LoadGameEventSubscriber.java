package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.LoadGameEvent;
import elite.companion.session.PlayerStats;

public class LoadGameEventSubscriber {

    public LoadGameEventSubscriber() {
        EventTracker.resetProcessed();
        EventBusManager.register(this);
    }

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerStats playerStats = new PlayerStats();
        playerStats.setPlayerName(event.getCommander());
        playerStats.setCurrentShip(event.getShip());
        playerStats.setCreditBalance(event.getCredits());
        EventBusManager.publish(playerStats);
    }

}

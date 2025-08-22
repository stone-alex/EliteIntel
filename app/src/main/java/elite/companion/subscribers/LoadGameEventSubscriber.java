package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.LoadGameEvent;
import elite.companion.session.PlayerStats;
import elite.companion.session.SessionTracker;

public class LoadGameEventSubscriber {

    public LoadGameEventSubscriber() {
        EventTracker.resetProcessed();
        EventBusManager.register(this);
    }

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerStats playerStats = SessionTracker.getPlayerStats();
        playerStats.setPlayerName(event.getCommander());
        playerStats.setCurrentShip(event.getShip());
        playerStats.setCreditBalance(event.getCredits());
        SessionTracker.setPlayerStats(playerStats);
    }

}

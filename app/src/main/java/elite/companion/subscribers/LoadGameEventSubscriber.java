package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.LoadGameEvent;
import elite.companion.session.PlayerStats;
import elite.companion.session.PublicSession;
import elite.companion.session.SystemSession;

import static elite.companion.Globals.SENSOR_READING;

public class LoadGameEventSubscriber {

    public LoadGameEventSubscriber() {
        EventTracker.resetProcessed();
        EventBusManager.register(this);
    }

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerStats playerStats = new PlayerStats();
        //playerStats.setPlayerName(event.getCommander());
        playerStats.setFuelLevel(event.getFuelLevel());
        playerStats.setPlayerName("Krondor");
        playerStats.setCurrentShip(event.getShip() + " designation " + event.getShipID());
        playerStats.setCreditBalance(event.getCredits());
        EventBusManager.publish(playerStats);
        SystemSession.getInstance().updateSession(SENSOR_READING, "New Game session started (debugging session)");
    }

}

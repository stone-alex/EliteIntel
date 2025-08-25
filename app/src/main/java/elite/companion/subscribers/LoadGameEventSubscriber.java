package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.LoadGameEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

import static elite.companion.session.PlayerSession.*;
import static elite.companion.session.SystemSession.SENSOR_READING;

public class LoadGameEventSubscriber {

    public LoadGameEventSubscriber() {
        EventTracker.resetProcessed();
        EventBusManager.register(this);
    }

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();

        playerSession.updateSession(SHIP_FUEL_LEVEL, event.getFuelLevel());
        playerSession.updateSession(PLAYER_NAME, /*event.getCommander()*/"Krondor");
        playerSession.updateSession(CURRENT_SHIP, event.getShip());
        playerSession.updateSession(CURRENT_SHIP_NAME, event.getShipName());
        playerSession.updateSession(PERSONAL_CREDITS_AVAILABLE, event.getCredits());


        SystemSession.getInstance().updateSession(SENSOR_READING, "New Game session started (debugging session)");
    }

}

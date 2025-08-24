package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.LoadGameEvent;
import elite.companion.session.PlayerStats;
import elite.companion.session.SessionTracker;

import static elite.companion.Globals.EXTERNAL_TRANSMISSION;

public class LoadGameEventSubscriber {

    public LoadGameEventSubscriber() {
        EventTracker.resetProcessed();
        EventBusManager.register(this);
    }

    @Subscribe
    public void onEvent(LoadGameEvent event) {
        PlayerStats playerStats = new PlayerStats();
        //playerStats.setPlayerName(event.getCommander());
        playerStats.setPlayerName("Krondor");
        playerStats.setCurrentShip(event.getShip());
        playerStats.setCreditBalance(event.getCredits());
        EventBusManager.publish(playerStats);
        SessionTracker.getInstance().updateSession(EXTERNAL_TRANSMISSION, "New Game debugging session started. Say hi to the user");
    }

}

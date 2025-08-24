package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.CommanderEvent;
import elite.companion.session.PlayerStats;
import elite.companion.session.PublicSession;

public class CommanderEventSubscriber {

    public CommanderEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onEvent(CommanderEvent event) {
        PlayerStats playerStats = PublicSession.getInstance().getPlayerStats();
        playerStats.setPlayerName(event.getName());
    }
}

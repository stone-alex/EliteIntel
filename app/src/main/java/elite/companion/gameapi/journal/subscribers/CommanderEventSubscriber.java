package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.CommanderEvent;
import elite.companion.session.PlayerSession;

import static elite.companion.session.PlayerSession.PLAYER_NAME;

public class CommanderEventSubscriber {

    public CommanderEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onEvent(CommanderEvent event) {
        PlayerSession session = PlayerSession.getInstance();
        session.updateSession(PLAYER_NAME, event.getName());
    }
}

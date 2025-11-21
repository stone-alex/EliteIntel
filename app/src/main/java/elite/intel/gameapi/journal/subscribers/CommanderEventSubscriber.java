package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.CommanderEvent;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class CommanderEventSubscriber {

    @Subscribe
    public void onEvent(CommanderEvent event) {
        PlayerSession session = PlayerSession.getInstance();
        String nikName = session.getPlayerName();
        session.setInGameName(event.getName());
    }
}

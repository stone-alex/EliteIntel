package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.ReputationEvent;
import elite.companion.session.PlayerSession;

@SuppressWarnings("unused")
public class ReputationSubscriber {

    @Subscribe
    public void onReputationEvent(ReputationEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.put(PlayerSession.REPUTATION, event.toJson());
    }
}

package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.ReputationEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class ReputationSubscriber {

    @Subscribe
    public void onReputationEvent(ReputationEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.put(PlayerSession.REPUTATION, event.toJson());
    }
}

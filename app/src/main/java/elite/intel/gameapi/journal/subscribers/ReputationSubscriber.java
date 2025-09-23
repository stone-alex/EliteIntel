package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.ReputationEvent;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class ReputationSubscriber {

    @Subscribe
    public void onReputationEvent(ReputationEvent event) {
        PlayerSession.getInstance().setReputation(event);
    }
}

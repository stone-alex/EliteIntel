package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.RedeemVoucherEvent;
import elite.companion.session.SystemSession;

public class RedeemVoucherSubscriber {

    @Subscribe
    public void onRedeemVoucherEvent(RedeemVoucherEvent event) {
        SystemSession.getInstance().sendToAiAnalysis(event.toJson());
    }
}

package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SendToGrokEvent;
import elite.companion.gameapi.journal.events.RedeemVoucherEvent;
import elite.companion.util.EventBusManager;

public class RedeemVoucherSubscriber {

    @Subscribe
    public void onRedeemVoucherEvent(RedeemVoucherEvent event) {
        EventBusManager.publish(new SendToGrokEvent(event.toJson()));
    }
}

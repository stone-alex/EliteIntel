package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.RedeemVoucherEvent;
import elite.companion.session.PlayerSession;
import elite.companion.util.EventBusManager;

public class RedeemVoucherSubscriber {

    @Subscribe
    public void onRedeemVoucherEvent(RedeemVoucherEvent event) {
        PlayerSession.getInstance().clearBounties();
        EventBusManager.publish(new SensorDataEvent(event.toJson()));
    }
}

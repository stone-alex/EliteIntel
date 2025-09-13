package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.RedeemVoucherEvent;
import elite.intel.session.PlayerSession;

public class RedeemVoucherSubscriber {

    @Subscribe
    public void onRedeemVoucherEvent(RedeemVoucherEvent event) {
        PlayerSession.getInstance().clearBounties();
        EventBusManager.publish(new SensorDataEvent(event.toJson()));
    }
}

package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.RedeemVoucherEvent;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;

public class RedeemVoucherSubscriber {

    @Subscribe
    public void onRedeemVoucherEvent(RedeemVoucherEvent event) {
        EventBusManager.publish(new SensorDataEvent(new DataDto("Bounty Payment Awarded", event).toJson()));
        PlayerSession.getInstance().clearBounties();
    }

    record DataDto(String info, RedeemVoucherEvent event) implements ToJsonConvertible {
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}

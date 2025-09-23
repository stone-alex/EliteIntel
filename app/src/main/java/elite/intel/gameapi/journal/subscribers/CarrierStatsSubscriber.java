package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.CarrierStatsEvent;
import elite.intel.session.PlayerSession;

public class CarrierStatsSubscriber {

    @Subscribe
    public void onCarrierStatsEvent(CarrierStatsEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setCarrierStats(event);
    }
}

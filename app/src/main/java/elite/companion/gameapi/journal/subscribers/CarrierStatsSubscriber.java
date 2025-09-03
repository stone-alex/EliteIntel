package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.CarrierStatsEvent;
import elite.companion.session.PlayerSession;

public class CarrierStatsSubscriber {

    @Subscribe
    public void onCarrierStatsEvent(CarrierStatsEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.onCarrierStats(event);
        playerSession.put(PlayerSession.CARRIER_NAME, event.getName());
        playerSession.put(PlayerSession.CARRIER_CALLSIGN, event.getCallsign());
    }
}

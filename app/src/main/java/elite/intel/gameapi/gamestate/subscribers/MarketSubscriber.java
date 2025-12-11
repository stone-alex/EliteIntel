package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.eddm.EdDnClient;
import elite.intel.gameapi.gamestate.dtos.GameEvents;
import elite.intel.session.PlayerSession;

public class MarketSubscriber {

    @Subscribe
    public void onMarketEvent(GameEvents.MarketEvent marketEvent) {
        PlayerSession.getInstance().saveMarket(marketEvent);
        EdDnClient edDnClient = EdDnClient.getInstance();
        edDnClient.uploadMarket(marketEvent);
    }
}

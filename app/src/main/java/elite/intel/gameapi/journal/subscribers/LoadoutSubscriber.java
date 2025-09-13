package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.session.PlayerSession;

import static elite.intel.session.PlayerSession.SHIP_LOADOUT_JSON;


public class LoadoutSubscriber {

    @Subscribe
    public void onLoadoutEvent(LoadoutEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.put(PlayerSession.CURRENT_SHIP_NAME, event.getShipName());
        playerSession.put(PlayerSession.CURRENT_SHIP, event.getShip());
        playerSession.put(PlayerSession.SHIP_CARGO_CAPACITY, event.getCargoCapacity());

        PlayerSession.getInstance().put(SHIP_LOADOUT_JSON, event.toJson());
    }
}

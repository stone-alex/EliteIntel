package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.LoadoutEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

import static elite.companion.session.SystemSession.SHIP_LOADOUT_JSON;

public class LoadoutSubscriber {

    @Subscribe
    public void onLoadoutEvent(LoadoutEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.updateSession(PlayerSession.CURRENT_SHIP_NAME, event.getShipName());
        playerSession.updateSession(PlayerSession.CURRENT_SHIP, event.getShip());
        playerSession.updateSession(PlayerSession.SHIP_CARGO_CAPACITY, event.getCargoCapacity());

        SystemSession.getInstance().updateSession(SHIP_LOADOUT_JSON, event.toJson());
    }
}

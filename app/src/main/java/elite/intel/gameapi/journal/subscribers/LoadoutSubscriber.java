package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.ShipManager;
import elite.intel.gameapi.journal.events.LoadoutEvent;
import elite.intel.session.PlayerSession;


public class LoadoutSubscriber {

    @Subscribe
    public void onLoadoutEvent(LoadoutEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.setCurrentShipName(event.getShipName());
        playerSession.setCurrentShip(event.getShip());
        playerSession.setShipCargoCapacity(event.getCargoCapacity());
        playerSession.setShipLoadout(event);

        ShipManager shipManager = ShipManager.getInstance();
        shipManager.save(event.getShipId(), event.getShipName(), event.getCargoCapacity(), event.getShipIdent());
    }
}

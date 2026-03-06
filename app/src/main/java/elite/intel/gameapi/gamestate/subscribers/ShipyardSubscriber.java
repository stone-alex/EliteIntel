package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.gamestate.dtos.GameEvents;

public class ShipyardSubscriber {

    @Subscribe public void onShipyardEvent(GameEvents.ShipyardEvent event) {
        //
    }
}

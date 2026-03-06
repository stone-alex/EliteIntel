package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.gamestate.dtos.GameEvents;

public class OutfittingSubscriber {

    @Subscribe public void onOutfittingEvent(GameEvents.OutfittingEvent event) {
        //
    }
}

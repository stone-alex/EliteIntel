package elite.companion.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.gamestate.events.GameEvents;
import elite.companion.session.PlayerSession;

public class CargoChangedEventSubscriber {

    @Subscribe
    public void onCargoChangedEvent(GameEvents.CargoEvent event) {
        PlayerSession.getInstance().put(PlayerSession.SHIP_CARGO, event.toJson());
    }
}

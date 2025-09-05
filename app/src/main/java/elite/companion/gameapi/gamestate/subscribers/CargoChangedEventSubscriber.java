package elite.companion.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.gamestate.events.GameEvents;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

public class CargoChangedEventSubscriber {

    public CargoChangedEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onCargoChangedEvent(GameEvents.CargoEvent event) {
        SystemSession.getInstance().put(PlayerSession.SHIP_CARGO, event.toJson());
    }
}

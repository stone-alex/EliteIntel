package elite.companion.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.util.EventBusManager;
import elite.companion.gameapi.gamestate.events.GameEvents;
import elite.companion.session.SystemSession;

public class CargoChangedEventSubscriber {

    public CargoChangedEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onCargoChangedEvent(GameEvents.CargoEvent event){
        SystemSession.getInstance().put(SystemSession.SHIP_CARGO, event.toJson());
    }
}

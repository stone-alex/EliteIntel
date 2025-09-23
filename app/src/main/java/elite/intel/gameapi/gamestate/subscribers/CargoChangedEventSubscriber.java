package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")//registered in SubscriberRegistration
public class CargoChangedEventSubscriber {

    @Subscribe
    public void onCargoChangedEvent(GameEvents.CargoEvent event) {
        PlayerSession.getInstance().setShipCargo(event);
    }
}

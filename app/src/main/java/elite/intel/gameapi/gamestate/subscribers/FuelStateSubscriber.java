package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.gamestate.events.GameEvents;
import elite.intel.session.PlayerSession;
import elite.intel.util.json.GsonFactory;

@SuppressWarnings("unused") //registered in SubscriberRegistration
public class FuelStateSubscriber {

    @Subscribe
    public void onStatusChange(GameEvents.StatusEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.put(PlayerSession.CURRENT_FUEL_STATUS, GsonFactory.getGson().toJson(event));
        //This method is called often but returns the same data most of the time.
    }
}

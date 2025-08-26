package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.LoadoutEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

import static elite.companion.session.SystemSession.LOADOUT_JSON;

public class LoadoutSubscriber {

    public LoadoutSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onLoadoutEvent(LoadoutEvent event) {
        PlayerSession session = PlayerSession.getInstance();
        session.updateSession(PlayerSession.CURRENT_SHIP_NAME, event.getShipName());
        SystemSession.getInstance().updateSession(LOADOUT_JSON, event.toJson());
        SystemSession.getInstance().setSensorData("Loadout updated: " + event.toJson());
    }
}

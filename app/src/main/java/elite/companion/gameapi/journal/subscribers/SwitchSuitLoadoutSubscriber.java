package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.SwitchSuitLoadoutEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;


@SuppressWarnings("unused")
public class SwitchSuitLoadoutSubscriber {

    @Subscribe
    public void onSwitchSuitLoadoutEvent(SwitchSuitLoadoutEvent event) {
        SystemSession.getInstance().put(PlayerSession.SUITE_LOADOUT_JSON, event.toJson());
        EventBusManager.publish(new SensorDataEvent("Suit Loadout: " + event.toJson()));
    }
}

package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.SwitchSuitLoadoutEvent;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;


@SuppressWarnings("unused")
public class SwitchSuitLoadoutSubscriber {

    @Subscribe
    public void onSwitchSuitLoadoutEvent(SwitchSuitLoadoutEvent event) {
        SystemSession.getInstance().put(PlayerSession.SUITE_LOADOUT_JSON, event.toJson());
        EventBusManager.publish(new SensorDataEvent("Suit Loadout: " + event.toJson()));
    }
}

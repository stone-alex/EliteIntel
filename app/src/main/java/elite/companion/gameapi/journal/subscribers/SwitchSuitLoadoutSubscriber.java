package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.SwitchSuitLoadoutEvent;
import elite.companion.session.SystemSession;

import static elite.companion.session.SystemSession.SUITE_LOADOUT_JSON;

public class SwitchSuitLoadoutSubscriber {

    public SwitchSuitLoadoutSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onSwitchSuitLoadoutEvent(SwitchSuitLoadoutEvent event) {
        SystemSession.getInstance().updateSession(SUITE_LOADOUT_JSON, event.toJson());
        SystemSession.getInstance().setSensorData("Suit Loadout: " + event.toJson());
    }
}

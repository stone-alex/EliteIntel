package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SendToGrokEvent;
import elite.companion.gameapi.journal.events.SwitchSuitLoadoutEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

import static elite.companion.session.SystemSession.SUITE_LOADOUT_JSON;

@SuppressWarnings("unused")
public class SwitchSuitLoadoutSubscriber {

    @Subscribe
    public void onSwitchSuitLoadoutEvent(SwitchSuitLoadoutEvent event) {
        SystemSession.getInstance().put(SUITE_LOADOUT_JSON, event.toJson());
        EventBusManager.publish(new SendToGrokEvent("Suit Loadout: " + event.toJson()));
    }
}

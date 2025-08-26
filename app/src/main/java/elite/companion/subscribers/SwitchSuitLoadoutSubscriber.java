package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.SwitchSuitLoadoutEvent;
import elite.companion.session.SystemSession;

public class SwitchSuitLoadoutSubscriber {

    public SwitchSuitLoadoutSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onSwitchSuitLoadoutEvent(SwitchSuitLoadoutEvent event) {
        SystemSession.getInstance().setSensorData("Switch Suit Loadout: " + event.toJson());
    }
}

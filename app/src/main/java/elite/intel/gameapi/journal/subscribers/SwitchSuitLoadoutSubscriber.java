package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.SwitchSuitLoadoutEvent;


@SuppressWarnings("unused")
public class SwitchSuitLoadoutSubscriber {

    @Subscribe
    public void onSwitchSuitLoadoutEvent(SwitchSuitLoadoutEvent event) {
        //NOTE: implement me
    }
}

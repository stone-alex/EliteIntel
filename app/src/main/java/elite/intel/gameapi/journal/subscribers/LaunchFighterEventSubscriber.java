package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.LaunchFighterEvent;
import elite.intel.session.Status;

public class LaunchFighterEventSubscriber {

    @Subscribe
    public void onLaunchFighterEvent(LaunchFighterEvent event) {
        Status.getInstance().setFighterOut(true);
    }
}

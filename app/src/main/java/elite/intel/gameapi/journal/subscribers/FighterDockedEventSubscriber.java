package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.DockFighterEvent;
import elite.intel.session.Status;

public class FighterDockedEventSubscriber {

    @Subscribe
    public void onFighterDockedEvent(DockFighterEvent event) {
        Status.getInstance().setFighterOut(false);
    }
}

package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.EngineerProgressEvent;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class EngineerProgressSubscriber {

    @Subscribe
    public void onEngineerProgressEvent(EngineerProgressEvent event) {
        //Need an augmented JSON that contains data about which engineer is doing what.
        SystemSession.getInstance().updateSession(SystemSession.ENGINEER_PROGRESS, event.toJson());
    }
}

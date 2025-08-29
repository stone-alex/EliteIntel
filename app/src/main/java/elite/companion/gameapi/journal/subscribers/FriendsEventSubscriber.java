package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.FriendsEvent;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class FriendsEventSubscriber {

    @Subscribe
    public void onFriendsEvent(FriendsEvent event) {
        String friendNamer = event.getName();
        String friendStatus = event.getStatus();
        SystemSession.getInstance().setConsumableData("Friend: " + friendNamer + " is " + friendStatus);
    }
}

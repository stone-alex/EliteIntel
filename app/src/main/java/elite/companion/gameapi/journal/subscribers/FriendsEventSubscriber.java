package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.FriendsEvent;
import elite.companion.util.EventBusManager;

@SuppressWarnings("unused")
public class FriendsEventSubscriber {

    @Subscribe
    public void onFriendsEvent(FriendsEvent event) {
        String friendNamer = event.getName();
        String friendStatus = event.getStatus();
        EventBusManager.publish(new SensorDataEvent("Friend: " + friendNamer + " is " + friendStatus));
    }
}

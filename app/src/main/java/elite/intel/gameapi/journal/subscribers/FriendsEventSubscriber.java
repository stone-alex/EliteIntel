package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.FriendsEvent;

@SuppressWarnings("unused")
public class FriendsEventSubscriber {

    @Subscribe
    public void onFriendsEvent(FriendsEvent event) {
        String friendNamer = event.getName();
        String friendStatus = event.getStatus();
        EventBusManager.publish(new SensorDataEvent("Friend: " + friendNamer + " is " + friendStatus));
    }
}

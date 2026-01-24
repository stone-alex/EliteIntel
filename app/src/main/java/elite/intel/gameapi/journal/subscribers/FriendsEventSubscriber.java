package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.FriendsEvent;

@SuppressWarnings("unused")
public class FriendsEventSubscriber {

    @Subscribe
    public void onFriendsEvent(FriendsEvent event) {
        EventBusManager.publish(new SensorDataEvent("Friend: " + event.getName() + " is " + event.getStatus(), "Notify User"));
    }
}

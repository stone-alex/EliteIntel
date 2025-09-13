package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.LaunchSRVEvent;

public class LaunchSRVSubscriber {

    @Subscribe
    public void onLaunchSRVEvent(LaunchSRVEvent event) {
        //implement launch SRV event
    }
}

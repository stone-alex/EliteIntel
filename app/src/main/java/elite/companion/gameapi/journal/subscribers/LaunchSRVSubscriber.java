package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.LaunchSRVEvent;

public class LaunchSRVSubscriber {

    @Subscribe
    public void onLaunchSRVEvent(LaunchSRVEvent event) {
        //implement launch SRV event
    }
}

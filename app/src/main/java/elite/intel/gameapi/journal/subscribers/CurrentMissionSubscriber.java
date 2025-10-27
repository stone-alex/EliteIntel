package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.journal.events.MissionsEvent;

public class CurrentMissionSubscriber {

    @Subscribe
    public void onCurrentMission(MissionsEvent event) {
        //NOTE: Not sure what to do with this yet.
    }
}

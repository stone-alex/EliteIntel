package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.HistoricalMissionScanner;
import elite.intel.gameapi.journal.events.MissionsEvent;

@SuppressWarnings("unused")
public class MissionsEventSubscriber {

    private final MissionManager missionManager = MissionManager.getInstance();
    private final HistoricalMissionScanner missionScanner = HistoricalMissionScanner.getInstance();

    @Subscribe /// NOTE: handled by MissingMissionMonitor
    public void onMissionsEventSubscriber(MissionsEvent event) {
        if (!event.getActive().isEmpty()) {
            EventBusManager.register(new MissionCriticalAnnouncementEvent(" We have %s outstanding missions".formatted(event.getActive().size())));
        }
    }
}

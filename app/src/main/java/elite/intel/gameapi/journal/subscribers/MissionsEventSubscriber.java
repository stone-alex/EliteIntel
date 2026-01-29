package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.HistoricalMissionScanner;
import elite.intel.gameapi.journal.events.MissionsEvent;
import elite.intel.ui.event.AppLogEvent;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class MissionsEventSubscriber {

    private final MissionManager missionManager = MissionManager.getInstance();
    private final HistoricalMissionScanner missionScanner = HistoricalMissionScanner.getInstance();

    @Subscribe /// NOTE: handled by MissingMissionMonitor
    public void onMissionsEventSubscriber(MissionsEvent event) {
        if (!event.getActive().isEmpty()) {
            EventBusManager.register(new MissionCriticalAnnouncementEvent(" We have %s outstanding missions".formatted(event.getActive().size())));
        }
        if (!event.getComplete().isEmpty() || !event.getFailed().isEmpty()) {
            // Removes old and completed missions from the database.
            Set<Long> existingMissionIds = missionManager.getMissions().keySet();
            Set<Long> completedOrFailedIDs = new HashSet<>();
            event.getComplete().forEach( m -> completedOrFailedIDs.add(m.getMissionID()));
            event.getFailed().forEach( m -> completedOrFailedIDs.add(m.getMissionID()));

            Set<Long> offset = existingMissionIds.stream()
                    .filter(completedOrFailedIDs::contains)
                    .collect(Collectors.toSet());

            if(offset.isEmpty()) return;
            offset.forEach(missionManager::remove);
            EventBusManager.publish(new AppLogEvent("Info: Removed " + offset.size() + " old mission data from DB."));
        }
    }
}

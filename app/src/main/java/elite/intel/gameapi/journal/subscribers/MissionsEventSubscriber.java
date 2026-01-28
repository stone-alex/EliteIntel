package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.HistoricalMissionScanner;
import elite.intel.gameapi.journal.events.MissionAcceptedEvent;
import elite.intel.gameapi.journal.events.MissionsEvent;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;
import elite.intel.util.StringUtls;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class MissionsEventSubscriber {

    private final MissionManager missionManager = MissionManager.getInstance();
    private final HistoricalMissionScanner missionScanner = HistoricalMissionScanner.getInstance();

    @Subscribe
    public void onMissionsEventSubscriber(MissionsEvent event) {
        if (event.getActive().isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No active missions!"));
            return;
        }

        List<Long> accepted = new ArrayList<>(event.getActive())
                .stream()
                .map(MissionsEvent.Mission::getMissionID)
                .toList();
        List<Long> databaseMissions = new ArrayList<>(missionManager.getMissions().keySet());

        Set<Long> filtered = new HashSet<>(accepted);
        databaseMissions.forEach(filtered::remove);

        List<MissionAcceptedEvent> missingMissions = missionScanner.scanForPendingAcceptedEvents(filtered);
        for (MissionAcceptedEvent mission : missingMissions) {
            EventBusManager.publish(new AiVoxResponseEvent(
                    "%s! i detected a %s mission that i haven't catalogued.".formatted(
                            StringUtls.player(PlayerSession.getInstance()),
                            mission.getName()
                    )
            ));
            missionManager.save(new MissionDto(mission));
        }
    }
}

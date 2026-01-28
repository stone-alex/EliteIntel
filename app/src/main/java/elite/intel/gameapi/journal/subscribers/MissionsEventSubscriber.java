package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.MissionAcceptedEvent;
import elite.intel.gameapi.journal.events.MissionsEvent;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.ui.event.AppLogEvent;

import java.util.*;

@SuppressWarnings("unused")
public class MissionsEventSubscriber {
    private final MissionManager missionManager = MissionManager.getInstance();

    @Subscribe
    public void onMissionsEventSubscriber(MissionsEvent event) {
        if (event.getActive().isEmpty()){
            EventBusManager.publish(new AiVoxResponseEvent("No active missions!"));
            return;
        }
        List<Long> accepted = new ArrayList<>(event.getActive())
                .stream()
                .map(MissionsEvent.Mission::getMissionID)
                .toList();
        List<Long> databaseMissions = new ArrayList<>(missionManager.getMissions().keySet());

        List<Long> filtered = new ArrayList<>(accepted);
        filtered.removeAll(databaseMissions);

        // todo: EventBus event used for debuggingz
        EventBusManager.publish(new AppLogEvent("MissionEvent Registered: accepted: "+event.getActive().size() + ", filtered: " + filtered.size()));
        for (MissionsEvent.Mission mission : event.getActive()) {
            if (filtered.contains(mission.getMissionID())){
                MissionAcceptedEvent dto = new MissionAcceptedEvent(mission.toJsonObject());
                System.out.println("\n ----- MissionAcceptedEvent objecet --- " + dto);
                missionManager.save(new MissionDto(dto));
            }
        }
    }
}

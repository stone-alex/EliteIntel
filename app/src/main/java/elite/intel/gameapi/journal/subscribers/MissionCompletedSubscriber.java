package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.MissionCompletedEvent;
import elite.intel.gameapi.journal.events.dto.BountyDto;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class MissionCompletedSubscriber {

    @Subscribe
    public void onMissionCompletedEvent(MissionCompletedEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        MissionDto mission = playerSession.getMission(event.getMissionID());
        if (mission == null) {
            return; // no mission in session storage. just exit.
        }
        String targetFaction = event.getTargetFaction();

        //we are done with this mission
        playerSession.removeMission(event.getMissionID());

        //do we have any more missions against this faction?
        Map<Long, MissionDto> missions = playerSession.getMissions();
        int countRemainingMissionsAgainstFaction = 0;
        for (MissionDto m : missions.values()) {
            if (m.getMissionTargetFaction().equalsIgnoreCase(targetFaction)) {
                countRemainingMissionsAgainstFaction++;
            }
        }

        EventBusManager.publish(new SensorDataEvent("Notify: Mission against Faction \"" + targetFaction + "\" Completed: " + event.toString()));
    }
}
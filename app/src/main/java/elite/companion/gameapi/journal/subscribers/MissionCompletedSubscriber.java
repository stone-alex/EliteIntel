package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.MissionCompletedEvent;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.session.PlayerSession;

import java.util.Map;

@SuppressWarnings("unused")
public class MissionCompletedSubscriber {

    @Subscribe
    public void onMissionCompletedEvent(MissionCompletedEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.removeMission(event.getMissionID());
        playerSession.clearMissionKills();
        String targetFaction = event.getTargetFaction();
        EventBusManager.publish(new SensorDataEvent("Mission against Faction:\"" + targetFaction + "\" Completed: " + event.toJson()));

        Map<Long, MissionDto> missions = playerSession.getMissions();
        int countRemainingMissionsAgainstFaction = 0;
        for (MissionDto mission : missions.values()) {
            if (mission.getFaction().equalsIgnoreCase(targetFaction)) countRemainingMissionsAgainstFaction++;
        }
        if (countRemainingMissionsAgainstFaction == 0) {
            playerSession.getTargetFactions().remove(targetFaction);
            playerSession.removeMission(event.getMissionID());
            playerSession.saveSession();
        }
    }
}
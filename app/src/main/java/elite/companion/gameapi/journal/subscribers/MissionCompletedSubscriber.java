package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.MissionCompletedEvent;
import elite.companion.gameapi.journal.events.dto.BountyDto;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.session.PlayerSession;

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

        // remove bounties for this mission (total profit from bounties is saved in a separate field)
        Set<BountyDto> bounties = playerSession.getBounties();
        List<BountyDto> temp = bounties.stream().toList();
        int killCount = mission.getKillCount();
        for (int i = 0; i < killCount; i++) {
            BountyDto bounty = temp.get(i);
            if (bounty.getVictimFaction().equalsIgnoreCase(mission.getMissionTargetFaction())) {
                playerSession.removeBounty(bounty);
            }
        }

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

        if (countRemainingMissionsAgainstFaction == 0) {
            playerSession.getTargetFactions().remove(targetFaction);
        }
        playerSession.saveSession();

        EventBusManager.publish(new SensorDataEvent("Mission against Faction:\"" + targetFaction + "\" Completed: " + event.toString()));
    }
}
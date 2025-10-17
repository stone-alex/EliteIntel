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

        // remove bounties for this mission (total profit from bounties is saved in a separate field)
        Set<BountyDto> bounties = playerSession.getBounties();
        List<BountyDto> temp = bounties.stream().toList();
        int killCount = mission.getKillCount();
        for (int i = 0; i < killCount; i++) {
            if(temp.isEmpty()) continue;
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
            Set<String> targetFactions = playerSession.getTargetFactions();
            Set<String> updatedSet = new HashSet<>();
            for(String faction : targetFactions) {
                if(faction.equalsIgnoreCase(targetFaction)) continue;
                updatedSet.add(faction);
            }
            playerSession.setTargetFactions(updatedSet);
        }


        EventBusManager.publish(new SensorDataEvent("Notify: Mission against Faction \"" + targetFaction + "\" Completed: " + event.toString()));
    }
}
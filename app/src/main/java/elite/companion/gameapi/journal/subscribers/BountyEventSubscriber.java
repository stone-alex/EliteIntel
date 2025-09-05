package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.BountyEvent;
import elite.companion.gameapi.journal.events.dto.MissionKillDto;
import elite.companion.session.PlayerSession;
import elite.companion.util.EventBusManager;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class BountyEventSubscriber {

    @Subscribe
    public void onBountyEvent(BountyEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.clearShipScans();
        List<BountyEvent.Reward> rewards = event.getRewards();
        StringBuilder sb = new StringBuilder();
        String killConfirmed = "";
        Set<String> targetFactions = playerSession.getTargetFactions();
        if (targetFactions.contains(event.getVictimFaction())) {
            killConfirmed = "Mission Kill Confirmed, ";
            MissionKillDto missionKillDto = new MissionKillDto();
            missionKillDto.setTargetFaction(event.getVictimFaction());
            missionKillDto.setVictimPilotName(event.getPilotNameLocalised());
            playerSession.addMissionKill(missionKillDto);
        } else {
            killConfirmed = "Kill Confirmed, ";
        }
        sb.append(killConfirmed);
        for (BountyEvent.Reward reward : rewards) {
            sb.append("Reward: ").append(reward.getReward()).append(" credits ");
            sb.append("From: " + reward.getFaction()).append(", ");
        }
        if (rewards.size() > 1) sb.append("Rewards sum: ").append(event.getTotalReward()).append(" credits. ");
        playerSession.addBounty(event.getTotalReward());

        sb.append("Total bounties collected: ").append(playerSession.getBountyCollectedThisSession()).append(" credits. ");
        EventBusManager.publish(new SensorDataEvent(sb.toString()));
    }
}

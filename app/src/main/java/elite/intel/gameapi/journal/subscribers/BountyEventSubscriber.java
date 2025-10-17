package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.BountyEvent;
import elite.intel.gameapi.journal.events.dto.BountyDto;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class BountyEventSubscriber {

    @Subscribe
    public void onBountyEvent(BountyEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.clearShipScans();

        BountyDto sessionData = new BountyDto();
        sessionData.setPilotName(event.getPilotName());
        sessionData.setVictimFaction(event.getVictimFaction());
        sessionData.setTarget(event.getTarget());
        sessionData.setTotalReward(event.getTotalReward());
        List<BountyDto.Reward> rewards = new ArrayList<>();
        for( BountyEvent.Reward reward : event.getRewards() ) {
            BountyDto.Reward r = new BountyDto.Reward();
            r.setFaction(reward.getFaction());
            r.setReward(reward.getReward());
            rewards.add(r);
        }
        sessionData.setRewards(rewards);
        playerSession.addBounty(sessionData);

        StringBuilder sb = new StringBuilder();
        String killConfirmed = "";
        Set<String> targetFactions = playerSession.getTargetFactions();

        if (!targetFactions.isEmpty() && targetFactions.contains(event.getVictimFaction())) {
            killConfirmed = " Mission Kill Confirmed, ";
        } else {
            killConfirmed = " Kill Confirmed, ";
        }
        sb.append(killConfirmed);
        for (BountyDto.Reward reward : rewards) {
            sb.append(" Reward: ").append(reward.getReward()).append(" credits ");
            sb.append(" From: " + reward.getFaction()).append(", ");
        }

        if (rewards.size() > 1) sb.append(" Rewards sum: ").append(event.getTotalReward()).append(" credits. ");
        playerSession.addBountyReward(event.getTotalReward());

        sb.append("Total bounties collected: ").append(playerSession.getBountyCollectedThisSession()).append(" credits. ");
        EventBusManager.publish(new AiVoxResponseEvent(sb.toString()));
    }
}

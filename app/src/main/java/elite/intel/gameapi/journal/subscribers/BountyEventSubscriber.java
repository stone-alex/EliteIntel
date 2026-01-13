package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.BountyEvent;
import elite.intel.gameapi.journal.events.dto.BountyDto;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class BountyEventSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final MissionManager missionManager = MissionManager.getInstance();

    @Subscribe
    public void onBountyEvent(BountyEvent event) {

        playerSession.clearShipScans();

        BountyDto sessionData = new BountyDto();
        sessionData.setPilotName(event.getPilotName());
        sessionData.setVictimFaction(event.getVictimFaction());
        sessionData.setTarget(event.getTarget());
        sessionData.setTotalReward(event.getTotalReward());
        List<BountyDto.Reward> rewards = new ArrayList<>();
        for (BountyEvent.Reward reward : event.getRewards()) {
            BountyDto.Reward r = new BountyDto.Reward();
            r.setFaction(reward.getFaction());
            r.setReward(reward.getReward());
            rewards.add(r);
        }
        sessionData.setRewards(rewards);
        playerSession.addBounty(sessionData);

        StringBuilder sb = new StringBuilder();
        Set<String> targetFactions = missionManager.getTargetFactions(missionManager.getPirateMissionTypes());
        if (!targetFactions.isEmpty() && targetFactions.contains(event.getVictimFaction())) {
            sb.append(" Mission Kill Confirmed, ");
        } else {
            sb.append(" Kill Confirmed, ");
        }

        long bountyCollected = rewards.stream().mapToLong(r -> r.getReward()).sum();
        if (rewards.size() > 0) sb.append(bountyCollected + " Bounty Claimed ");
        playerSession.addBountyReward(event.getTotalReward());
        EventBusManager.publish(new AiVoxResponseEvent(sb.toString()));
    }
}

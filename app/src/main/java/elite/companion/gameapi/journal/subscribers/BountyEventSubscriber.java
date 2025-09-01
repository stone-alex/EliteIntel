package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SendToGrokEvent;
import elite.companion.gameapi.journal.events.BountyEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

import java.util.List;

@SuppressWarnings("unused")
public class BountyEventSubscriber {

    @Subscribe
    public void onBountyEvent(BountyEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        List<BountyEvent.Reward> rewards = event.getRewards();
        StringBuilder sb = new StringBuilder();
        String killConfirmed = "";
        if (event.getVictimFaction().equalsIgnoreCase(String.valueOf(systemSession.get(SystemSession.TARGET_FACTION_NAME)))) {
            //VoiceGenerator.getInstance().speak("Mission Kill Confirmed, " + Ranks.getPlayerHonorific()+"!");
            killConfirmed  = "Mission Kill Confirmed, ";
        } else {
            //VoiceGenerator.getInstance().speak("Kill Confirmed, " + Ranks.getPlayerHonorific()+"!");
            killConfirmed  = "Kill Confirmed, ";
        }
        sb.append(killConfirmed);
        for (BountyEvent.Reward reward : rewards) {
            sb.append("Reward: ").append(reward.getReward()).append(" credits ");
            sb.append("From: " + reward.getFaction()).append(", ");
        }
        if(rewards.size() > 1) sb.append("Rewards sum: ").append(event.getTotalReward()).append(" credits. ");
        //sb.append("Loosing Faction: ").append(event.getVictimFaction());
        systemSession.addBounty(event.getTotalReward());

        sb.append("Total bounties collected: ").append(systemSession.getBountyCollectedThisSession()).append(" credits. ");
        EventBusManager.publish(new SendToGrokEvent(sb.toString()));
    }
}

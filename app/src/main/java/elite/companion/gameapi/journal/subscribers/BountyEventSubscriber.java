package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.journal.events.BountyEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.Ranks;

import java.util.List;

@SuppressWarnings("unused")
public class BountyEventSubscriber {

    @Subscribe
    public void onBountyEvent(BountyEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        List<BountyEvent.Reward> rewards = event.getRewards();
        StringBuilder sb = new StringBuilder();
        if (event.getVictimFaction().equalsIgnoreCase(String.valueOf(systemSession.get(SystemSession.TARGET_FACTION_NAME)))) {
            VoiceGenerator.getInstance().speak("Kill Confirmed, " + Ranks.getPlayerHonorific()+"!");
        }
        for (BountyEvent.Reward reward : rewards) {
            sb.append("Reward: ").append(reward.getReward()).append(" credits ");
            sb.append("From: " + reward.getFaction()).append(", ");
        }
        sb.append("Total: ").append(event.getTotalReward()).append(" credits. ");
        sb.append("Loosing Faction: ").append(event.getVictimFaction());

        systemSession.addBounty(event.getTotalReward());

        sb.append("Total bounty collected: ").append(systemSession.getBountyCollectedThisSession()).append(" credits. ");
        systemSession.sendToAiAnalysis(sb.toString());
    }
}

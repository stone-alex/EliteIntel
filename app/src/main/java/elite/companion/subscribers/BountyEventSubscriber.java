package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.BountyEvent;
import elite.companion.session.SystemSession;

import java.util.List;

public class BountyEventSubscriber {

    public BountyEventSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onBountyEvent(BountyEvent event) {
        List<BountyEvent.Reward> rewards = event.getRewards();
        StringBuilder sb = new StringBuilder();
        for(BountyEvent.Reward reward : rewards){
            sb.append("Reward: ").append(reward.getReward()).append(" credits ");
            sb.append("From: "+reward.getFaction()).append(", ");
        }
        sb.append("Total: ").append(event.getTotalReward()).append(" credits. ");
        sb.append("Loosing Faction: ").append(event.getVictimFaction());


        SystemSession systemSession = SystemSession.getInstance();
        systemSession.addBounty(event.getTotalReward());

        sb.append("Total bounty collected: ").append(systemSession.getBountyCollectedThisSession()).append(" credits. ");

        systemSession.setSensorData(sb.toString());
    }
}

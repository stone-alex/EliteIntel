package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.journal.events.CommitCrimeEvent;

public class CommitCrimeEventSubscriber {

    @Subscribe
    public void onCommitCrimeEvent(CommitCrimeEvent event) {
        StringBuilder sb = new StringBuilder();

        sb.append(" WARNING: Faction ");
        sb.append(event.getFaction());
        sb.append(" issued bounty of ").append(event.getBounty()).append(" credits ");
        sb.append(" for ").append(event.getCrimeType());
        sb.append(" against ");
        sb.append(event.getVictimLocalised());

        EventBusManager.publish(new MissionCriticalAnnouncementEvent(sb.toString()));
    }
}

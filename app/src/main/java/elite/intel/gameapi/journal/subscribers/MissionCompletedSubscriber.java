package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.MissionCompletedEvent;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class MissionCompletedSubscriber {

    @Subscribe
    public void onMissionCompletedEvent(MissionCompletedEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        MissionDto mission = playerSession.getMission(event.getMissionID());

        if (mission == null) {
            return; // no mission in session storage. just exit.
        }

        playerSession.removeMission(event.getMissionID());
        String targetFaction = event.getTargetFaction();
        EventBusManager.publish(new SensorDataEvent("Notify: Mission against Faction \"" + targetFaction + "\" Completed: " + event));
    }
}
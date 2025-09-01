package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.MissionCompletedEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

@SuppressWarnings("unused")
public class MissionCompletedSubscriber {

    @Subscribe
    public void onMissionCompletedEvent(MissionCompletedEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.removePirateMission(event.getMissionID());
        String targetFaction = String.valueOf(systemSession.get(SystemSession.TARGET_FACTION_NAME));
        EventBusManager.publish(new SensorDataEvent("Mission against Faction:\""+targetFaction+"\" Completed: " + event.toJson()));
    }
}
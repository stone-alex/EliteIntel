package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.MissionCompletedEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

@SuppressWarnings("unused")
public class MissionCompletedSubscriber {

    @Subscribe
    public void onMissionCompletedEvent(MissionCompletedEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.removePirateMission(event.getMissionID());
        String targetFaction = String.valueOf(playerSession.get(PlayerSession.TARGET_FACTION_NAME));
        EventBusManager.publish(new SensorDataEvent("Mission against Faction:\""+targetFaction+"\" Completed: " + event.toJson()));
    }
}
package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.MissionAcceptedEvent;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.session.PlayerSession;

@SuppressWarnings("unused")
public class MissionAcceptedHandler {

    @Subscribe
    public void onMissionAcceptedEvent(MissionAcceptedEvent event) {

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.addTargetFaction(event.getTargetFaction());
        playerSession.addMission(new MissionDto(event));
        EventBusManager.publish(new SensorDataEvent("Mission Accepted: " + event.toJson()));
    }
}
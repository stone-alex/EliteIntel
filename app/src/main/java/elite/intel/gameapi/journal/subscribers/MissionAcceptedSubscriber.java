package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.MissionAcceptedEvent;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;

@SuppressWarnings("unused")
public class MissionAcceptedSubscriber {

    @Subscribe
    public void onMissionAcceptedEvent(MissionAcceptedEvent event) {

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.addMission(new MissionDto(event));
        EventBusManager.publish(new SensorDataEvent("Mission Accepted: " + event.toJson()));
    }
}
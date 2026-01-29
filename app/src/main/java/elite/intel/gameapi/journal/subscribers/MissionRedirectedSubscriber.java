package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.MissionRedirectedEvent;
import elite.intel.gameapi.journal.events.dto.MissionDto;

@SuppressWarnings("unused")
public class MissionRedirectedSubscriber {
    private MissionManager missionManager = MissionManager.getInstance();

    @Subscribe
    public void onMissionRedirectedSubscriber(MissionRedirectedEvent event) {
        MissionDto mission = missionManager.getMission(event.getMissionID());
        if (!event.getNewDestinationStation().isEmpty()) {
            mission.setDestinationStation(event.getNewDestinationStation());
        }
        if (!event.getNewDestinationSystem().isEmpty()) {
            mission.setDestinationSystem(event.getNewDestinationSystem());
        }
        missionManager.save(mission);
        EventBusManager.publish(new SensorDataEvent("Notify: New Destination for mission \"" + event.getLocalisedName()
                + "\": " + mission.toJsonObject(),
                "Notify user of mission update, provide the new destination system and station if present in the data received."));
    }

}

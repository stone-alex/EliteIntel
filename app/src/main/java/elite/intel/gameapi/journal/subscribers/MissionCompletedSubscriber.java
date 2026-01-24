package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.MissionType;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.MissionCompletedEvent;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;

import static elite.intel.gameapi.MissionType.MISSION_PIRATE_MASSACRE;
import static elite.intel.gameapi.MissionType.MISSION_PIRATE_MASSACRE_WING;
import static elite.intel.util.StringUtls.removeNameEnding;

@SuppressWarnings("unused")
public class MissionCompletedSubscriber {

    @Subscribe
    public void onMissionCompletedEvent(MissionCompletedEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        MissionDto mission = playerSession.getMission(event.getMissionID());
        MissionType missionType = MissionManager.getInstance()
                .getMissionType(removeNameEnding(event.getName()));

        if (mission == null) {
            return; // no mission in session storage. just exit.
        }
        if (MISSION_PIRATE_MASSACRE.equals(missionType) || MISSION_PIRATE_MASSACRE_WING.equals(missionType)) {
            playerSession.removeMission(event.getMissionID());
            String targetFaction = event.getTargetFaction();
            EventBusManager.publish(new SensorDataEvent("Notify: Mission against Faction \"" + targetFaction + "\" Completed: " + event, "Notify user of a successful mission completion, provide detailed summary from the data received."));
        }
        if (false) {
            /*
                TODO: If user is on a mission with staging.
                 i.e. a return address when completed, add the return address/details to the database
                 and delete when the "Missions" event is fired
             */
        }

    }
}
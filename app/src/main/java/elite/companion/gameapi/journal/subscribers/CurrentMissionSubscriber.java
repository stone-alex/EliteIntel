package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.MissionsEvent;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.session.PlayerSession;

import java.util.List;


/**
 * Handles the list of missions we get when the user logs in to the game.
 * There is no useful information here, except the expiration time.
 * The expiration time is represented in the number of seconds.
 *
 */
public class CurrentMissionSubscriber {

    @Subscribe
    public void onCurrentMission(MissionsEvent event) {
/*
        PlayerSession session = PlayerSession.getInstance();
        List<MissionsEvent.Mission> activeMissions = event.getActive();
        for (MissionsEvent.Mission mission : activeMissions) {
            MissionDto storedMission = session.getMission(mission.getMissionID());
            if (mission.getExpires() < 1) {
                session.removeMission(mission.getMissionID());
                EventBusManager.publish(new VoiceProcessEvent(" Mission: " + storedMission.getMissionDescription() + " expired."));
            }
        }
*/

    }
}

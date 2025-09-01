package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.MissionAcceptedEvent;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

@SuppressWarnings("unused")
public class MissionAcceptedHandler {

    @Subscribe
    public void onMissionAcceptedEvent(MissionAcceptedEvent event) {

        MissionDto mission = new MissionDto(event);
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.addMission(mission);
        EventBusManager.publish(new SensorDataEvent("Mission Accepted: " + mission.toJson()));
    }
}


/**
 * Example data
 * <p>
 * {
 * "timestamp": "2025-08-29T02:11:54Z",
 * "event": "MissionAccepted",
 * "Faction": "Baminyi Bridge Group",
 * "Name": "Mission_Massacre",
 * "LocalisedName": "Kill Xue Davokje Blue Cartel faction Pirates",
 * "TargetType": "$MissionUtil_FactionTag_Pirate;",
 * "TargetType_Localised": "Pirates",
 * "TargetFaction": "Xue Davokje Blue Cartel",
 * "KillCount": 25,
 * "DestinationSystem": "Xue Davokje",
 * "DestinationStation": "Ludwig Struve Gateway",
 * "Expiry": "2025-08-29T18:54:18Z",
 * "Wing": false,
 * "Influence": "++",
 * "Reputation": "++",
 * "Reward": 30222222,
 * "MissionID": 1027206784
 * }
 *
 *
 */
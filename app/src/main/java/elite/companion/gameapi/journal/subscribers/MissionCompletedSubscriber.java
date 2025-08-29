package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.MissionCompletedEvent;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class MissionCompletedSubscriber {

    @Subscribe
    public void onMissionCompletedEvent(MissionCompletedEvent event) {
        //event.getTargetFaction()
        SystemSession.getInstance().removePirateMission(event.getMissionID());
    }
}

/**
 * Example data:
 * <p>
 * {
 * "timestamp": "2025-08-28T05:54:08Z",
 * "event": "MissionCompleted",
 * "Faction": "Baminyi Defence Force",
 * "Name": "Mission_Massacre_name",
 * "LocalisedName": "Kill Xue Davokje Blue Cartel faction Pirates",
 * "MissionID": 1027207084,
 * "TargetType": "$MissionUtil_FactionTag_Pirate;",
 * "TargetType_Localised": "Pirates",
 * "TargetFaction": "Xue Davokje Blue Cartel",
 * "KillCount": 15,
 * "DestinationSystem": "Xue Davokje",
 * "DestinationStation": "Tsiolkovsky Orbital",
 * "Reward": 20716110,
 * "FactionEffects": [
 * {
 * "Faction": "",
 * "Effects": [
 * {
 * "Effect": "$MISSIONUTIL_Interaction_Summary_EP_down;",
 * "Effect_Localised": "The economic status of $#MinorFaction; has declined in the $#System; system.",
 * "Trend": "DownBad"
 * }
 * ],
 * "Influence": [
 * {
 * "SystemAddress": 3107643658946,
 * "Trend": "DownBad",
 * "Influence": "+"
 * }
 * ],
 * "ReputationTrend": "DownBad",
 * "Reputation": "+"
 * },
 * {
 * "Faction": "Baminyi Defence Force",
 * "Effects": [
 * {
 * "Effect": "$MISSIONUTIL_Interaction_Summary_EP_up;",
 * "Effect_Localised": "The economic status of $#MinorFaction; has improved in the $#System; system.",
 * "Trend": "UpGood"
 * }
 * ],
 * "Influence": [
 * {
 * "SystemAddress": 7269097612681,
 * "Trend": "UpGood",
 * "Influence": "++"
 * }
 * ],
 * "ReputationTrend": "UpGood",
 * "Reputation": "++"
 * }
 * ]
 * }
 *
 *
 */

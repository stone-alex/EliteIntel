package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.*;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

import static elite.companion.gameapi.MissionTypes.PIRATES;

@SuppressWarnings("unused")
public class PirateMissionSubscriber {

    @Subscribe
    public void onEvent(BaseEvent event) {
        PlayerSession session = PlayerSession.getInstance();
        if (event instanceof MissionAcceptedEvent) {
            MissionAcceptedEvent mae = (MissionAcceptedEvent) event;
            if (mae.getTargetTypeLocalised().equals(PIRATES.getMissionType())) {
                MissionDto mission = new MissionDto(mae);
                session.put(PlayerSession.TARGET_FACTION_NAME, mission.getMissionTargetFaction());
                session.addPirateMission(mission);
            }
        } else if (event instanceof BountyEvent) {
            session.addPirateBounty((BountyEvent) event);
        } else if (event instanceof MissionCompletedEvent ||
                event instanceof MissionAbandonedEvent ||
                event instanceof MissionFailedEvent
        ) {
            long missionID = ((MissionCompletedEvent) event).getMissionID(); // Assumes shared interface
            session.removePirateMission(missionID);
        }
    }
}
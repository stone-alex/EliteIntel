package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.BaseEvent;
import elite.companion.gameapi.journal.events.BountyEvent;
import elite.companion.gameapi.journal.events.MissionAcceptedEvent;
import elite.companion.gameapi.journal.events.MissionCompletedEvent;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.session.SystemSession;

import static elite.companion.gameapi.MissionTypes.PIRATES;

@SuppressWarnings("unused")
public class PirateMissionSubscriber {

    @Subscribe
    public void onEvent(BaseEvent event) {
        SystemSession session = SystemSession.getInstance();
        if (event instanceof MissionAcceptedEvent) {
            MissionAcceptedEvent mae = (MissionAcceptedEvent) event;
            if (mae.getTargetTypeLocalised().equals(PIRATES.getMissionType())) {
                session.addPirateMission(new MissionDto(mae));
            }
        } else if (event instanceof BountyEvent) {
            session.addPirateBounty((BountyEvent) event);
        } else if (event instanceof MissionCompletedEvent
//                ||
//                event instanceof MissionAbandonedEvent ||
//                event instanceof MissionFailedEvent
        ) {
            long missionID = ((MissionCompletedEvent) event).getMissionID(); // Assumes shared interface
            session.removePirateMission(missionID);
        }
    }
}
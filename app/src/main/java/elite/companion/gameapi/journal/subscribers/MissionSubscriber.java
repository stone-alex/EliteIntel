package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.*;
import elite.companion.gameapi.journal.events.dto.MissionDto;
import elite.companion.session.PlayerSession;

import static elite.companion.gameapi.MissionTypes.PIRATES;

@SuppressWarnings("unused")
public class MissionSubscriber {

    @Subscribe
    public void onEvent(BaseEvent event) {
        PlayerSession session = PlayerSession.getInstance();
        if (event instanceof MissionAcceptedEvent) {
            MissionAcceptedEvent mae = (MissionAcceptedEvent) event;
            if (mae.getTargetTypeLocalised().equals(PIRATES.getMissionType())) {
                MissionDto mission = new MissionDto(mae);

                session.addTargetFaction(mission.getMissionTargetFaction());
                session.addMission(mission);
            }
        } else if (event instanceof MissionCompletedEvent ||
                event instanceof MissionAbandonedEvent ||
                event instanceof MissionFailedEvent
        ) {
            assert event instanceof MissionCompletedEvent;
            long missionID = ((MissionCompletedEvent) event).getMissionID(); // Assumes shared interface
            session.removeMission(missionID);
        }
    }
}
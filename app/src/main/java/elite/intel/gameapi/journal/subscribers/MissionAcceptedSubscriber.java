package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.MissionManager;
import elite.intel.db.managers.PirateMissionDataManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.MissionType;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.MissionAcceptedEvent;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;

import static elite.intel.gameapi.MissionType.MISSION_PIRATE_MASSACRE;
import static elite.intel.gameapi.MissionType.MISSION_PIRATE_MASSACRE_WING;

@SuppressWarnings("unused")
public class MissionAcceptedSubscriber {

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final MissionManager missionManager = MissionManager.getInstance();

    private static void genericMission(MissionAcceptedEvent event, MissionManager missionManager) {
        if (event != null) {
            missionManager.save(new MissionDto(event));
            EventBusManager.publish(new SensorDataEvent("Mission Accepted: " + event.toJson(), "Provide key mission parameters as a summary. Ignore unimportant fields such as timestamps, timeToLive, missionID etc."));
        }
    }

    private static void processPirateMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        PirateMissionDataManager pirateMissionDataManager = PirateMissionDataManager.getInstance();

        String destinationSystem = event.getDestinationSystem();
        String targetFaction = event.getTargetFaction();

        String providerStarSystem = playerSession.getPrimaryStarName();
        String missionProviderFaction = event.getFaction();

        int factionId = pirateMissionDataManager.updateTargetFaction(destinationSystem, targetFaction);
        pirateMissionDataManager.updateProviderFaction(providerStarSystem, factionId, missionProviderFaction);

        playerSession.addMission(new MissionDto(event));
        EventBusManager.publish(new SensorDataEvent("Mission Accepted: " + event.toJson(),  "Provide key mission parameters as a summary. Ignore unimportant fields such as timestamps, timeToLive, missionID etc."));
    }

    @Subscribe
    public void onMissionAcceptedEvent(MissionAcceptedEvent event) {
        MissionType missionType = missionManager.getMissionType(event.getName());

        if (MISSION_PIRATE_MASSACRE.equals(missionType) || MISSION_PIRATE_MASSACRE_WING.equals(missionType)) {
            processPirateMission(event, playerSession);
        }
        else {
            genericMission(event, missionManager);
        }
    }

}
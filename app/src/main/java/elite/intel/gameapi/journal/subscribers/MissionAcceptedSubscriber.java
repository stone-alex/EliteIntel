package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.db.managers.PirateMissionDataManager;
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
        ///TODO determine mission type and make instance of corresponding mission manager.

        if(true){ // change this to if pirate mission
            processPirateMission(event, playerSession);
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
        EventBusManager.publish(new SensorDataEvent("Mission Accepted: " + event.toJson()));
    }
}
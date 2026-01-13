package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.managers.MissionManager;
import elite.intel.db.managers.PirateMissionDataManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.MissionType;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.MissionAcceptedEvent;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.AppLogEvent;

import static elite.intel.gameapi.MissionType.MISSION_PIRATE_MASSACRE;
import static elite.intel.gameapi.MissionType.MISSION_PIRATE_MASSACRE_WING;

@SuppressWarnings("unused")
public class MissionAcceptedSubscriber {
/*
    faction='The Imperial Inquisition'
    Missionname='Mission_Mining'
    MissionlocalisedName='Mine 408 Units of Silver'
    MissiontargetType='null'
    MissiontargetTypeLocalised='null'
    MissiontargetFaction='null'
    MissionkillCount=0
    MissiondestinationSystem='Brestla'
    MissiondestinationStation='i Sola Prospect'
    MissiondestinationSettlement='null'
    Missionexpiry='2026-01-17T04:22:43Z'
    Missionwing=true
    Missioninfluence='+'
    Missionreputation='+'
    Missionreward=50000000
    MissionmissionID=1042004406
    Missiontarget='null'
    Missioncount=408
    MissioncommodityName='Silver'
 */

    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final MissionManager missionManager = MissionManager.getInstance();

    @Subscribe
    public void onMissionAcceptedEvent(MissionAcceptedEvent event) {
        MissionType missionType = missionManager.getMissionType(event.getName());

        if (event.getName().contains("Assassinate")) processAssassinationMission(event, playerSession);
        else if (event.getName().contains("Piracy")) processPiracyMission(event, playerSession);
        else if (event.getName().contains("Courier")) processCourierMission(event, playerSession);
        else if (event.getName().contains("Delivery")) processDeliveryMission(event, playerSession);
        else if (event.getName().contains("PassengerBulk")
                || event.getName().contains("PassengerVIP")
                || event.getName().contains("LongDistanceExpedition")) processPassengerMission(event, playerSession);
        else if (event.getName().contains("Altruism")) processAltruismMission(event, playerSession);
        else if (event.getName().contains("Salvage")) processSalvageMission(event, playerSession);
        else if (event.getName().contains("Collect")) processCollectMission(event, playerSession);
        else if (event.getName().contains("Mining")) processMiningMission(event, playerSession);
        else if (event.getName().contains("OnFoot")) processOnFootMission(event, playerSession);
        else if (event.getName().contains("MassacreWing")) processPirateMission(event, playerSession);

        if (MISSION_PIRATE_MASSACRE.equals(missionType) || MISSION_PIRATE_MASSACRE_WING.equals(missionType)) {
            processPirateMission(event, playerSession);
        }
    }

    private void processMiningMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        String message = "Accepted Mining Mission! "+ event.getLocalisedName() + ". ";
        EventBusManager.publish(new AiVoxResponseEvent(message));
        EventBusManager.publish(new AppLogEvent(message));
    }

    private void processPiracyMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        String message = "Accepted Piracy Mission! "+ event.getLocalisedName() + ". ";
        EventBusManager.publish(new AiVoxResponseEvent(message));
        EventBusManager.publish(new AppLogEvent(message));
    }

    private void processAssassinationMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        String message = "Accepted Assassination Mission! "+ event.getLocalisedName() + ". ";
        EventBusManager.publish(new AiVoxResponseEvent(message));
        EventBusManager.publish(new AppLogEvent(message));

    }

    private static void processPassengerMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        String message = "Accepted Passanger Mission! "+ event.getLocalisedName() + ". ";
        EventBusManager.publish(new AiVoxResponseEvent(message));
        EventBusManager.publish(new AppLogEvent(message));
    }

    private static void processAltruismMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        String message = "Accepted Altruism Mission! "+ event.getLocalisedName() + ". ";
        EventBusManager.publish(new AiVoxResponseEvent(message));
        EventBusManager.publish(new AppLogEvent(message));
    }

    private static void processCourierMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        String message = "Accepted Courier Mission! "+ event.getLocalisedName() + ". ";
        EventBusManager.publish(new AiVoxResponseEvent(message));
        EventBusManager.publish(new AppLogEvent(message));
    }

    private static void processDeliveryMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        String message = "Accepted Delivery Mission! "+ event.getLocalisedName() + ". ";
        EventBusManager.publish(new AiVoxResponseEvent(message));
        EventBusManager.publish(new AppLogEvent(message));
    }

    private static void processSalvageMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        String message = "Accepted Salavge Mission! "+ event.getLocalisedName() + ". ";
        EventBusManager.publish(new AiVoxResponseEvent(message));
        EventBusManager.publish(new AppLogEvent(message));
    }

    private static void processCollectMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        String message = "Accepted Collection Mission! "+ event.getLocalisedName() + ". ";
        EventBusManager.publish(new AiVoxResponseEvent(message));
        EventBusManager.publish(new AppLogEvent(message));
    }

    private static void processOnFootMission(MissionAcceptedEvent event, PlayerSession playerSession) {
        String message = "Accepted On Foot Mission! "+ event.getLocalisedName() + ". ";
        EventBusManager.publish(new AiVoxResponseEvent(message));
        EventBusManager.publish(new AppLogEvent(message));
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
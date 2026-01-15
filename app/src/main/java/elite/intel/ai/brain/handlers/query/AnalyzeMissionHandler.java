package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.db.dao.MissionDao;
import elite.intel.db.managers.MissionManager;
import elite.intel.gameapi.journal.events.dto.MissionDto;
import elite.intel.session.PlayerSession;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AnalyzeMissionHandler implements QueryHandler {
    private final MissionManager missionManager = MissionManager.getInstance();

    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        Collection<MissionDto> missionDto = missionManager.getMissions().values();
        JsonArray combatMissions = new JsonArray();
        JsonArray passengerMissions = new JsonArray();
        JsonArray miningMissions = new JsonArray();
        JsonArray altruismMissions = new JsonArray();
        JsonArray deliveryMissions = new JsonArray();
        JsonArray otherMissions = new JsonArray();

        for (MissionDto id : missionDto) {
            MissionDto mission = PlayerSession.getInstance().getMission(id.getMissionId());
            if (mission == null) continue;
            System.out.println("For Mission loop: " + mission.toJson());
           /* switch (missionManager.getMissionType(mission.getEventType())) {
                case MISSION_DELIVERY:
                case MISSION_COURIER_SERVICE:
                case MISSION_COURIER_ELECTIONS:
                case MISSION_COURIER_OUTBREAK:
                case MISSION_COURIER_EXPANSION:
                case MISSION_COURIER: {
                    deliveryMissions.add(mission.toJsonObject());
                    break;
                }
                case MISSION_PASSENGERBULK:
                case MISSION_SIGHTSEEING_WHISTLEBLOWER_ELECTION:
                case MISSION_LONGDISTANCEEXPEDITION_EXPLORER_BOOM:
                case MISSION_PASSENGERVIP:
                case MISSION_SIGHTSEEING_TOURIST_BOOM:
                case MISSION_SIGHTSEEING:
                case MISSION_SIGHTSEEING_CRIMINAL_CIVILWAR:
                case MISSION_SIGHTSEEING_SCIENTIST_CIVILWAR:
                case MISSION_SIGHTSEEING_TERRORIST_CIVILWAR:
                case MISSION_PASSENGERBULK_PRISONEROFWAR_LEAVING:
                case MISSION_PASSENGERBULK_AIDWORKER_ARRIVING:
                case MISSION_PASSENGERVIP_TERRORIST_CIVILWAR: {
                    passengerMissions.add(mission.toJsonObject());
                    break;
                }
                case MISSION_ALTRUISMCREDITS_OUTBREAK:
                case MISSION_ALTRUISM:
                case MISSION_ALTRUISMCREDITS: {
                    altruismMissions.add(mission.toJsonObject());
                    break;
                }
                case MISSION_ASSASSINATE_RANKEMP:
                case MISSION_ASSASSINATE_RANKFED:
                case MISSION_PIRATE_MASSACRE_WING:
                case MISSION_ASSASSINATE_PLANETARY_EXPANSION:
                case MISSION_PIRATE_MASSACRE:
                case MISSION_PIRACY_ANARCHY : {
                    combatMissions.add(mission.toJsonObject());
                    break;
                }
                case MISSION_MINING: miningMissions.add(mission.toJsonObject());
                default: otherMissions.add(mission.toJsonObject());
            }*/
        }
        JsonObject jsonData = new JsonObject();
/*
        jsonData.add("CombatMissions",  combatMissions);
        jsonData.add("PassengerMissions", passengerMissions);
        jsonData.add("MiningMissions", miningMissions);
        jsonData.add("AltruismMissions", altruismMissions);
        jsonData.add("DeliveryMissions", deliveryMissions);
        jsonData.add("OtherMissions",  otherMissions);
*/
        System.out.println("----  AnalyzeMissionHandle: " + jsonData);
        return jsonData;
    }
}

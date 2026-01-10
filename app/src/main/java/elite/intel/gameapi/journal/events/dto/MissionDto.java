package elite.intel.gameapi.journal.events.dto;

import com.google.gson.JsonObject;
import elite.intel.gameapi.MissionTypes;
import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.gameapi.journal.events.MissionAcceptedEvent;
import elite.intel.util.json.GsonFactory;

public class MissionDto extends BaseJsonDto {
    private String faction;
    private MissionTypes missionType;
    private String missionDescription;
    private String missionTarget;
    private String missionTargetFaction;
    private int killCount; // number of kills required to complete the mission
    private long reward;
    private String destinationSystem;
    private boolean isReputationIncrease;
    private boolean influenceIncrease;
    private long missionId;


    public MissionDto(MissionAcceptedEvent event) {
        if(event != null) {
            setMissionId(event.getMissionID());
            setMissionProvider(event.getFaction());
            setMissionType(toMissionType(event.getName()));
            setMissionDescription(event.getLocalisedName());
            setMissionTarget(event.getTargetTypeLocalised());
            setMissionTargetFaction(event.getTargetFaction());
            setKillCount(event.getKillCount());
            setReward(event.getReward());
            setDestinationSystem(event.getDestinationSystem());
            setReputationIncrease(event.getReputation() != null && "++".equals(event.getReputation()));
            setInfluence(event.getInfluence() != null && "++".equals(event.getInfluence()));
        }
    }

    private MissionTypes toMissionType(String name) {
        //TODO: Fix me. Assign different mission types based on event name
/*        if("Delivery".equals(name)) {
            return MissionTypes.DELIVERY;
        } else if("Passenger".equals(name)) {
            return MissionTypes.PASSENGER;
        }*/
        return MissionTypes.PIRATES;
    }

    public void setMissionId(long missionID) {
        this.missionId = missionID;
    }

    public void setInfluence(boolean b) {
        this.influenceIncrease = b;
    }

    public void setReputationIncrease(boolean b) {
        this.isReputationIncrease = b;
    }

    public void setDestinationSystem(String destinationSystem) {
        this.destinationSystem = destinationSystem;
    }

    public void setReward(long reward) {
        this.reward = reward;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public void setMissionTargetFaction(String targetFaction) {
        this.missionTargetFaction = targetFaction;
    }

    public void setMissionTarget(String targetTypeLocalised) {
        this.missionTarget = targetTypeLocalised;
    }

    public void setMissionDescription(String localisedName) {
        this.missionDescription = localisedName;
    }

    public void setMissionType(MissionTypes name) {
        this.missionType = name;
    }

    public void setMissionProvider(String faction) {
        this.faction = faction;
    }

    public String getFaction() {
        return faction;
    }

    public MissionTypes getMissionType() {
        return missionType;
    }

    public String getMissionDescription() {
        return missionDescription;
    }

    public String getMissionTarget() {
        return missionTarget;
    }

    public String getMissionTargetFaction() {
        return missionTargetFaction;
    }

    public int getKillCount() {
        return killCount;
    }

    public long getReward() {
        return reward;
    }

    public String getDestinationSystem() {
        return destinationSystem;
    }

    public boolean isReputationIncrease() {
        return isReputationIncrease;
    }

    public boolean isInfluenceIncrease() {
        return influenceIncrease;
    }

    public long getMissionId() {
        return missionId;
    }

    public String getEventType() {
        return "Mission";
    }

    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }
}

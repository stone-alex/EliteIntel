package elite.intel.gameapi.journal.events.dto;

import com.google.gson.JsonObject;
import elite.intel.gameapi.MissionTypes;
import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.gameapi.journal.events.MissionAcceptedEvent;
import elite.intel.util.json.GsonFactory;

public class MissionDto extends BaseJsonDto {
    /*
    Parameters:
        Name: name of mission
        LocalisedName: the mission name as displayed to the user
        Faction: faction offering mission
        MissionID
        Influence: effect on influence (None/Low/Med/High)
        Reputation: effect on reputation (None/Low/Med/High)
        Reward: expected cash reward
        Wing: bool
    */
    private MissionTypes missionType;
    private String faction;
    private String destinationSystem;
    private long reward;
    private boolean influenceIncrease;
    private boolean isReputationIncrease;

    // Pirate / kill mission
    private String missionDescription;
    private String missionTarget;
    private String missionTargetFaction;
    private int killCount; // number of kills required to complete the mission
    /*
    Optional Parameters (depending on mission type)
        Commodity: commodity type
        Count: number required / to deliver
        Donation: contracted donation (as string) (for altruism missions)
        Donated: actual donation (as int)
        Target: name of target
        TargetType: type of target
        TargetFaction: target's faction
        KillCount: number of targets
        Expiry: mission expiry time, in ISO 8601
        DestinationSystem
        DestinationStation
        DestinationSettlement
        NewDestinationSystem (if it has been redirected)
        NewDestinationStation (if redirected)
        PassengerCount
        PassengerVIPs: bool
        PassengerWanted: bool
        PassengerType: eg Tourist, Soldier, Explorer,...
    * */
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
        for ( MissionTypes type : MissionTypes.values()) {
            if (type.getMissionType().equalsIgnoreCase(name)){
                return type;
            }
        }
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

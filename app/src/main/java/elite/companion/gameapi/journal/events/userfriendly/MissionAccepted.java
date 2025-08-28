package elite.companion.gameapi.journal.events.userfriendly;

import com.google.gson.Gson;
import elite.companion.gameapi.journal.events.BaseEvent;
import elite.companion.gameapi.journal.events.MissionAcceptedEvent;
import elite.companion.gameapi.journal.events.ScannedEvent;

import java.time.Duration;

public class MissionAccepted extends BaseEvent {
    private String faction;
    private String missionType;
    private String missionDescription;
    private String missionTarget;
    private String missionTargetFaction;
    private int killCount;
    private long reward;
    private String destinationSystem;
    private boolean isReputationIncrease;
    private boolean influenceIncrease;
    private long missionId;


    public MissionAccepted(MissionAcceptedEvent event) {
        super(event.getTimestamp(), 1, Duration.ofSeconds(30), MissionAccepted.class.getName());
        setMissionId(event.getMissionID());
        setMissionProvider(event.getFaction());
        setMissionType(event.getName());
        setMissionDescription(event.getLocalisedName());
        setMissionTarget(event.getTargetTypeLocalised());
        setMissionTargetFaction(event.getTargetFaction());
        setKillCount(event.getKillCount());
        setReward(event.getReward());
        setDestinationSystem(event.getDestinationSystem());
        setReputationIncrease(event.getReputation() != null && "++".equals(event.getReputation()));
        setInfluence(event.getInfluence() != null && "++".equals(event.getInfluence()));
    }

    private void setMissionId(long missionID) {
        this.missionId = missionID;
    }

    private void setInfluence(boolean b) {
        this.influenceIncrease =b;
    }

    private void setReputationIncrease(boolean b) {
        this.isReputationIncrease = b;
    }

    private void setDestinationSystem(String destinationSystem) {
        this.destinationSystem = destinationSystem;
    }

    private void setReward(long reward) {
        this.reward = reward;
    }

    private void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    private void setMissionTargetFaction(String targetFaction) {
        this.missionTargetFaction = targetFaction;
    }

    private void setMissionTarget(String targetTypeLocalised) {
        this.missionTarget = targetTypeLocalised;
    }

    private void setMissionDescription(String localisedName) {
        this.missionDescription = localisedName;
    }

    private void setMissionType(String name) {
        this.missionType = name;
    }

    private void setMissionProvider(String faction) {
        this.faction = faction;
    }

    public String getFaction() {
        return faction;
    }

    public String getMissionType() {
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

    public String toJson() {
        return new Gson().toJson(this);
    }
}

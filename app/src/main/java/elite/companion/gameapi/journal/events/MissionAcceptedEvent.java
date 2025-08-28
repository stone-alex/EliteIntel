package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;

import java.time.Duration;
import java.util.StringJoiner;

public class MissionAcceptedEvent extends BaseEvent {
    @SerializedName("Faction")
    private String faction;

    @SerializedName("Name")
    private String name;

    @SerializedName("LocalisedName")
    private String localisedName;

    @SerializedName("TargetType")
    private String targetType;

    @SerializedName("TargetType_Localised")
    private String targetTypeLocalised;

    @SerializedName("TargetFaction")
    private String targetFaction;

    @SerializedName("KillCount")
    private int killCount;

    @SerializedName("DestinationSystem")
    private String destinationSystem;

    @SerializedName("DestinationStation")
    private String destinationStation;

    @SerializedName("Expiry")
    private String expiry;

    @SerializedName("Wing")
    private boolean wing;

    @SerializedName("Influence")
    private String influence;

    @SerializedName("Reputation")
    private String reputation;

    @SerializedName("Reward")
    private long reward;

    @SerializedName("MissionID")
    private long missionID;

    public MissionAcceptedEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), MissionAcceptedEvent.class.getName());
    }

    // Getters
    public String getFaction() {
        return faction;
    }

    public String getName() {
        return name;
    }

    public String getLocalisedName() {
        return localisedName;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetTypeLocalised() {
        return targetTypeLocalised;
    }

    public String getTargetFaction() {
        return targetFaction;
    }

    public int getKillCount() {
        return killCount;
    }

    public String getDestinationSystem() {
        return destinationSystem;
    }

    public String getDestinationStation() {
        return destinationStation;
    }

    public String getExpiry() {
        return expiry;
    }

    public boolean isWing() {
        return wing;
    }

    public String getInfluence() {
        return influence;
    }

    public String getReputation() {
        return reputation;
    }

    public long getReward() {
        return reward;
    }

    public long getMissionID() {
        return missionID;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }

    @Override
    public String toString() {
        return new StringJoiner("Mission accepted: ")
                .add("faction='" + faction + "'")
                .add("name='" + name + "'")
                .add("localisedName='" + localisedName + "'")
                .add("targetType='" + targetType + "'")
                .add("targetTypeLocalised='" + targetTypeLocalised + "'")
                .add("targetFaction='" + targetFaction + "'")
                .add("killCount=" + killCount)
                .add("destinationSystem='" + destinationSystem + "'")
                .add("destinationStation='" + destinationStation + "'")
                .add("expiry='" + expiry + "'")
                .add("wing=" + wing)
                .add("influence='" + influence + "'")
                .add("reputation='" + reputation + "'")
                .add("reward=" + reward)
                .add("missionID=" + missionID)
                .toString();
    }
}
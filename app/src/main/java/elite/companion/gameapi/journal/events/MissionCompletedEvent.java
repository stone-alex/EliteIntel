package elite.companion.gameapi.journal.events;

import com.google.gson.annotations.SerializedName;
import elite.companion.util.TimestampFormatter;

import java.time.Duration;
import java.util.List;
import java.util.StringJoiner;

public class MissionCompletedEvent extends BaseEvent {
    @SerializedName("Faction")
    private String faction;

    @SerializedName("Name")
    private String name;

    @SerializedName("LocalisedName")
    private String localisedName;

    @SerializedName("MissionID")
    private long missionID;

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

    @SerializedName("Reward")
    private long reward;

    @SerializedName("FactionEffects")
    private List<FactionEffect> factionEffects;

    public MissionCompletedEvent(String timestamp) {
        super(timestamp, 1, Duration.ofSeconds(30), MissionCompletedEvent.class.getName());
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

    public long getMissionID() {
        return missionID;
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

    public long getReward() {
        return reward;
    }

    public List<FactionEffect> getFactionEffects() {
        return factionEffects;
    }

    public String getFormattedTimestamp(boolean useLocalTime) {
        return TimestampFormatter.formatTimestamp(getTimestamp().toString(), useLocalTime);
    }

    public static class FactionEffect {
        @SerializedName("Faction")
        private String faction;

        @SerializedName("Effects")
        private List<Effect> effects;

        @SerializedName("Influence")
        private List<Influence> influence;

        @SerializedName("ReputationTrend")
        private String reputationTrend;

        @SerializedName("Reputation")
        private String reputation;

        public String getFaction() {
            return faction;
        }

        public List<Effect> getEffects() {
            return effects;
        }

        public List<Influence> getInfluence() {
            return influence;
        }

        public String getReputationTrend() {
            return reputationTrend;
        }

        public String getReputation() {
            return reputation;
        }
    }

    public static class Effect {
        @SerializedName("Effect")
        private String effect;

        @SerializedName("Effect_Localised")
        private String effectLocalised;

        @SerializedName("Trend")
        private String trend;

        public String getEffect() {
            return effect;
        }

        public String getEffectLocalised() {
            return effectLocalised;
        }

        public String getTrend() {
            return trend;
        }
    }

    public static class Influence {
        @SerializedName("SystemAddress")
        private long systemAddress;

        @SerializedName("Trend")
        private String trend;

        @SerializedName("Influence")
        private String influence;

        public long getSystemAddress() {
            return systemAddress;
        }

        public String getTrend() {
            return trend;
        }

        public String getInfluence() {
            return influence;
        }
    }

    @Override
    public String toString() {
        return new StringJoiner("Mission completed: ")
                .add("faction='" + faction + "'")
                .add("name='" + name + "'")
                .add("localisedName='" + localisedName + "'")
                .add("missionID=" + missionID)
                .add("targetType='" + targetType + "'")
                .add("targetTypeLocalised='" + targetTypeLocalised + "'")
                .add("targetFaction='" + targetFaction + "'")
                .add("killCount=" + killCount)
                .add("destinationSystem='" + destinationSystem + "'")
                .add("destinationStation='" + destinationStation + "'")
                .add("reward=" + reward)
                .add("factionEffects=" + factionEffects)
                .toString();
    }
}
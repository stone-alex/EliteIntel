package elite.intel.search.eddn.schemas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FsdJumpMessage {

    @SerializedName("event")
    private final String event = "FSDJump";
    @SerializedName("StarPos")
    List<Double> starPos;
    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("StarSystem")
    private String starSystem;
    @SerializedName("SystemAddress")
    private long systemAddress;
    @SerializedName("Body")
    private String body;

    @SerializedName("BodyID")
    private Long bodyID;

    @SerializedName("BodyType")
    private String bodyType;

    @SerializedName("SystemAllegiance")
    private String systemAllegiance;

    @SerializedName("SystemEconomy")
    private String systemEconomy;

    @SerializedName("SystemSecondEconomy")
    private String systemSecondEconomy;

    @SerializedName("SystemGovernment")
    private String systemGovernment;

    @SerializedName("SystemSecurity")
    private String systemSecurity;

    @SerializedName("Population")
    private Long population;

    @SerializedName("SystemFaction")
    private SystemFaction systemFaction;

    @SerializedName("Factions")
    private List<Faction> factions;

    @SerializedName("Conflicts")
    private List<Conflict> conflicts;

    @SerializedName("Powers")
    private List<String> powers;

    @SerializedName("PowerplayState")
    private String powerplayState;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getEvent() {
        return event;
    }

    public String getStarSystem() {
        return starSystem;
    }

    public void setStarSystem(String starSystem) {
        this.starSystem = starSystem;
    }

    public long getSystemAddress() {
        return systemAddress;
    }

    public void setSystemAddress(long systemAddress) {
        this.systemAddress = systemAddress;
    }

    public List<Double> getStarPos() {
        return starPos;
    }

    public void setStarPos(List<Double> starPos) {
        this.starPos = starPos;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getBodyID() {
        return bodyID;
    }

    public void setBodyID(Long bodyID) {
        this.bodyID = bodyID;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getSystemAllegiance() {
        return systemAllegiance;
    }

    public void setSystemAllegiance(String systemAllegiance) {
        this.systemAllegiance = systemAllegiance;
    }

    public String getSystemEconomy() {
        return systemEconomy;
    }

    public void setSystemEconomy(String systemEconomy) {
        this.systemEconomy = systemEconomy;
    }

    public String getSystemSecondEconomy() {
        return systemSecondEconomy;
    }

    public void setSystemSecondEconomy(String systemSecondEconomy) {
        this.systemSecondEconomy = systemSecondEconomy;
    }

    public String getSystemGovernment() {
        return systemGovernment;
    }

    public void setSystemGovernment(String systemGovernment) {
        this.systemGovernment = systemGovernment;
    }

    public String getSystemSecurity() {
        return systemSecurity;
    }

    public void setSystemSecurity(String systemSecurity) {
        this.systemSecurity = systemSecurity;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public SystemFaction getSystemFaction() {
        return systemFaction;
    }

    public void setSystemFaction(SystemFaction systemFaction) {
        this.systemFaction = systemFaction;
    }

    public List<Faction> getFactions() {
        return factions;
    }

    public void setFactions(List<Faction> factions) {
        this.factions = factions;
    }

    public List<Conflict> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<Conflict> conflicts) {
        this.conflicts = conflicts;
    }

    public List<String> getPowers() {
        return powers;
    }

    public void setPowers(List<String> powers) {
        this.powers = powers;
    }

    public String getPowerplayState() {
        return powerplayState;
    }

    public void setPowerplayState(String powerplayState) {
        this.powerplayState = powerplayState;
    }

    public static class SystemFaction {
        @SerializedName("Name")
        private String name;

        @SerializedName("FactionState")
        private String factionState;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFactionState() {
            return factionState;
        }

        public void setFactionState(String factionState) {
            this.factionState = factionState;
        }
    }

    public static class Faction {
        @SerializedName("Name")
        private String name;

        @SerializedName("Allegiance")
        private String allegiance;

        @SerializedName("Government")
        private String government;

        @SerializedName("Influence")
        private Double influence;

        @SerializedName("Happiness")
        private String happiness;

        @SerializedName("FactionState")
        private String factionState;

        @SerializedName("ActiveStates")
        private List<ActiveState> activeStates;

        @SerializedName("PendingStates")
        private List<StateTrend> pendingStates;

        @SerializedName("RecoveringStates")
        private List<StateTrend> recoveringStates;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAllegiance() {
            return allegiance;
        }

        public void setAllegiance(String allegiance) {
            this.allegiance = allegiance;
        }

        public String getGovernment() {
            return government;
        }

        public void setGovernment(String government) {
            this.government = government;
        }

        public Double getInfluence() {
            return influence;
        }

        public void setInfluence(Double influence) {
            this.influence = influence;
        }

        public String getHappiness() {
            return happiness;
        }

        public void setHappiness(String happiness) {
            this.happiness = happiness;
        }

        public String getFactionState() {
            return factionState;
        }

        public void setFactionState(String factionState) {
            this.factionState = factionState;
        }

        public List<ActiveState> getActiveStates() {
            return activeStates;
        }

        public void setActiveStates(List<ActiveState> activeStates) {
            this.activeStates = activeStates;
        }

        public List<StateTrend> getPendingStates() {
            return pendingStates;
        }

        public void setPendingStates(List<StateTrend> pendingStates) {
            this.pendingStates = pendingStates;
        }

        public List<StateTrend> getRecoveringStates() {
            return recoveringStates;
        }

        public void setRecoveringStates(List<StateTrend> recoveringStates) {
            this.recoveringStates = recoveringStates;
        }
    }

    public static class ActiveState {
        @SerializedName("State")
        private String state;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }

    public static class StateTrend {
        @SerializedName("State")
        private String state;

        @SerializedName("Trend")
        private Integer trend;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Integer getTrend() {
            return trend;
        }

        public void setTrend(Integer trend) {
            this.trend = trend;
        }
    }

    public static class Conflict {
        @SerializedName("WarType")
        private String warType;

        @SerializedName("Status")
        private String status;

        @SerializedName("Faction1")
        private FactionSide faction1;

        @SerializedName("Faction2")
        private FactionSide faction2;

        public String getWarType() {
            return warType;
        }

        public void setWarType(String warType) {
            this.warType = warType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public FactionSide getFaction1() {
            return faction1;
        }

        public void setFaction1(FactionSide faction1) {
            this.faction1 = faction1;
        }

        public FactionSide getFaction2() {
            return faction2;
        }

        public void setFaction2(FactionSide faction2) {
            this.faction2 = faction2;
        }
    }

    public static class FactionSide {
        @SerializedName("Name")
        private String name;

        @SerializedName("Stake")
        private String stake;

        @SerializedName("WonDays")
        private Integer wonDays;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStake() {
            return stake;
        }

        public void setStake(String stake) {
            this.stake = stake;
        }

        public Integer getWonDays() {
            return wonDays;
        }

        public void setWonDays(Integer wonDays) {
            this.wonDays = wonDays;
        }
    }
}
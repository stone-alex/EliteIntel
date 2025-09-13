package elite.intel.gameapi.journal.events;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import elite.intel.util.json.GsonFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class FSDJumpEvent extends BaseEvent {
    @SerializedName("Taxi")
    private boolean taxi;

    @SerializedName("Multicrew")
    private boolean multicrew;

    @SerializedName("StarSystem")
    private String starSystem;

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("StarPos")
    private float[] starPos;

    @SerializedName("SystemAllegiance")
    private String systemAllegiance;

    @SerializedName("SystemEconomy")
    private String systemEconomy;

    @SerializedName("SystemEconomy_Localised")
    private String systemEconomyLocalised;

    @SerializedName("SystemSecondEconomy")
    private String systemSecondEconomy;

    @SerializedName("SystemSecondEconomy_Localised")
    private String systemSecondEconomyLocalised;

    @SerializedName("SystemGovernment")
    private String systemGovernment;

    @SerializedName("SystemGovernment_Localised")
    private String systemGovernmentLocalised;

    @SerializedName("SystemSecurity")
    private String systemSecurity;

    @SerializedName("SystemSecurity_Localised")
    private String systemSecurityLocalised;

    @SerializedName("Population")
    private long population;

    @SerializedName("Body")
    private String body;

    @SerializedName("BodyID")
    private int bodyId;

    @SerializedName("BodyType")
    private String bodyType;

    @SerializedName("ControllingPower")
    private String controllingPower;

    @SerializedName("Powers")
    private List<String> powers;

    @SerializedName("PowerplayState")
    private String powerplayState;

    @SerializedName("PowerplayStateControlProgress")
    private float powerplayStateControlProgress;

    @SerializedName("PowerplayStateReinforcement")
    private int powerplayStateReinforcement;

    @SerializedName("PowerplayStateUndermining")
    private int powerplayStateUndermining;

    @SerializedName("JumpDist")
    private float jumpDist;

    @SerializedName("FuelUsed")
    private float fuelUsed;

    @SerializedName("FuelLevel")
    private float fuelLevel;

    @SerializedName("Factions")
    private List<Faction> factions;

    @SerializedName("SystemFaction")
    private SystemFaction systemFaction;

    public FSDJumpEvent(JsonObject json) {
        super(json.get("timestamp").getAsString(), 1, Duration.ofSeconds(30), "FSDJump");
        FSDJumpEvent event = GsonFactory.getGson().fromJson(json, FSDJumpEvent.class);
        this.taxi = event.taxi;
        this.multicrew = event.multicrew;
        this.starSystem = event.starSystem;
        this.systemAddress = event.systemAddress;
        this.starPos = event.starPos;
        this.systemAllegiance = event.systemAllegiance;
        this.systemEconomy = event.systemEconomy;
        this.systemEconomyLocalised = event.systemEconomyLocalised;
        this.systemSecondEconomy = event.systemSecondEconomy;
        this.systemSecondEconomyLocalised = event.systemSecondEconomyLocalised;
        this.systemGovernment = event.systemGovernment;
        this.systemGovernmentLocalised = event.systemGovernmentLocalised;
        this.systemSecurity = event.systemSecurity;
        this.systemSecurityLocalised = event.systemSecurityLocalised;
        this.population = event.population;
        this.body = event.body;
        this.bodyId = event.bodyId;
        this.bodyType = event.bodyType;
        this.controllingPower = event.controllingPower;
        this.powers = event.powers;
        this.powerplayState = event.powerplayState;
        this.powerplayStateControlProgress = event.powerplayStateControlProgress;
        this.powerplayStateReinforcement = event.powerplayStateReinforcement;
        this.powerplayStateUndermining = event.powerplayStateUndermining;
        this.jumpDist = event.jumpDist;
        this.fuelUsed = event.fuelUsed;
        this.fuelLevel = event.fuelLevel;
        this.factions = event.factions;
        this.systemFaction = event.systemFaction;
    }

    @Override
    public String getEventType() {
        return "FSDJump";
    }

    @Override
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return GsonFactory.toJsonObject(this);
    }

    public boolean isTaxi() {
        return taxi;
    }

    public boolean isMulticrew() {
        return multicrew;
    }

    public String getStarSystem() {
        return starSystem;
    }

    public long getSystemAddress() {
        return systemAddress;
    }

    public float[] getStarPos() {
        return starPos;
    }

    public String getSystemAllegiance() {
        return systemAllegiance;
    }

    public String getSystemEconomyLocalised() {
        return systemEconomyLocalised;
    }

    public String getSystemSecondEconomyLocalised() {
        return systemSecondEconomyLocalised;
    }

    public String getSystemGovernmentLocalised() {
        return systemGovernmentLocalised;
    }

    public String getSystemSecurityLocalised() {
        return systemSecurityLocalised;
    }

    public long getPopulation() {
        return population;
    }

    public String getBody() {
        return body;
    }

    public int getBodyId() {
        return bodyId;
    }

    public String getBodyType() {
        return bodyType;
    }

    public String getControllingPower() {
        return controllingPower;
    }

    public List<String> getPowers() {
        return powers;
    }

    public String getPowerplayState() {
        return powerplayState;
    }

    public float getPowerplayStateControlProgress() {
        return powerplayStateControlProgress;
    }

    public int getPowerplayStateReinforcement() {
        return powerplayStateReinforcement;
    }

    public int getPowerplayStateUndermining() {
        return powerplayStateUndermining;
    }

    public float getJumpDist() {
        return jumpDist;
    }

    public float getFuelUsed() {
        return fuelUsed;
    }

    public float getFuelLevel() {
        return fuelLevel;
    }

    public List<Faction> getFactions() {
        return factions;
    }

    public SystemFaction getSystemFaction() {
        return systemFaction;
    }

    public static class Faction {
        @SerializedName("Name")
        private String name;

        @SerializedName("FactionState")
        private String factionState;

        @SerializedName("Government")
        private String government;

        @SerializedName("Influence")
        private float influence;

        @SerializedName("Allegiance")
        private String allegiance;

        @SerializedName("Happiness")
        private String happiness;

        @SerializedName("Happiness_Localised")
        private String happinessLocalised;

        @SerializedName("MyReputation")
        private float myReputation;

        @SerializedName("ActiveStates")
        private List<ActiveState> activeStates;

        public String getName() {
            return name;
        }

        public String getFactionState() {
            return factionState;
        }

        public String getGovernment() {
            return government;
        }

        public float getInfluence() {
            return influence;
        }

        public String getAllegiance() {
            return allegiance;
        }

        public String getHappinessLocalised() {
            return happinessLocalised;
        }

        public float getMyReputation() {
            return myReputation;
        }

        public List<ActiveState> getActiveStates() {
            return activeStates;
        }
    }

    public static class ActiveState {
        @SerializedName("State")
        private String state;

        public String getState() {
            return state;
        }
    }

    public static class SystemFaction {
        @SerializedName("Name")
        private String name;

        @SerializedName("FactionState")
        private String factionState;

        public String getName() {
            return name;
        }

        public String getFactionState() {
            return factionState;
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FSDJumpEvent.class.getSimpleName() + "[", "]")
                .add("taxi=" + taxi)
                .add("multicrew=" + multicrew)
                .add("starSystem='" + starSystem + "'")
                .add("systemAddress=" + systemAddress)
                .add("starPos=" + Arrays.toString(starPos))
                .add("systemAllegiance='" + systemAllegiance + "'")
                .add("systemEconomy='" + systemEconomy + "'")
                .add("systemEconomyLocalised='" + systemEconomyLocalised + "'")
                .add("systemSecondEconomy='" + systemSecondEconomy + "'")
                .add("systemSecondEconomyLocalised='" + systemSecondEconomyLocalised + "'")
                .add("systemGovernment='" + systemGovernment + "'")
                .add("systemGovernmentLocalised='" + systemGovernmentLocalised + "'")
                .add("systemSecurity='" + systemSecurity + "'")
                .add("systemSecurityLocalised='" + systemSecurityLocalised + "'")
                .add("population=" + population)
                .add("body='" + body + "'")
                .add("bodyId=" + bodyId)
                .add("bodyType='" + bodyType + "'")
                .add("controllingPower='" + controllingPower + "'")
                .add("powers=" + powers)
                .add("powerplayState='" + powerplayState + "'")
                .add("powerplayStateControlProgress=" + powerplayStateControlProgress)
                .add("powerplayStateReinforcement=" + powerplayStateReinforcement)
                .add("powerplayStateUndermining=" + powerplayStateUndermining)
                .add("jumpDist=" + jumpDist)
                .add("fuelUsed=" + fuelUsed)
                .add("fuelLevel=" + fuelLevel)
                .add("factions=" + factions)
                .add("systemFaction=" + systemFaction)
                .toString();
    }
}
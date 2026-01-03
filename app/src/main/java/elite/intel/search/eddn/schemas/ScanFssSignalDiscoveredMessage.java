package elite.intel.search.eddn.schemas;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScanFssSignalDiscoveredMessage {

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("event")
    private final String event = "FSSSignalDiscovered";

    @SerializedName("StarSystem")
    private String starSystem;  // REQUIRED by schema

    @SerializedName("StarPos")
    private List<Double> starPos;  // REQUIRED [x,y,z]

    @SerializedName("SystemAddress")
    private long systemAddress;

    @SerializedName("horizons")
    private Boolean horizons;

    @SerializedName("odyssey")
    private Boolean odyssey;

    @SerializedName("signals")
    private List<Signal> signals;

    public static class Signal {

        @SerializedName("timestamp")
        private String timestamp;

        @SerializedName("SignalName")
        private String signalName;

        @SerializedName("SignalType")
        private String signalType;

        @SerializedName("IsStation")
        private Boolean isStation;

        @SerializedName("USSType")
        private String ussType;

        @SerializedName("SpawningState")
        private String spawningState;

        @SerializedName("SpawningFaction")
        private String spawningFaction;

        @SerializedName("ThreatLevel")
        private Integer threatLevel;

        // Setters
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public void setSignalName(String signalName) { this.signalName = signalName; }
        public void setSignalType(String signalType) { this.signalType = signalType; }
        public void setIsStation(Boolean isStation) { this.isStation = isStation; }
        public void setUssType(String ussType) { this.ussType = ussType; }
        public void setSpawningState(String spawningState) { this.spawningState = spawningState; }
        public void setSpawningFaction(String spawningFaction) { this.spawningFaction = spawningFaction; }
        public void setThreatLevel(Integer threatLevel) { this.threatLevel = threatLevel; }
    }

    // Setters
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setStarSystem(String starSystem) { this.starSystem = starSystem; }
    public void setStarPos(List<Double> starPos) { this.starPos = starPos; }
    public void setSystemAddress(long systemAddress) { this.systemAddress = systemAddress; }
    public void setHorizons(Boolean horizons) { this.horizons = horizons; }
    public void setOdyssey(Boolean odyssey) { this.odyssey = odyssey; }
    public void setSignals(List<Signal> signals) { this.signals = signals; }
}
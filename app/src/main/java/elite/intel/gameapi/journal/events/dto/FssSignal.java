package elite.intel.gameapi.journal.events.dto;

import elite.intel.gameapi.gamestate.events.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

public class FssSignal extends BaseJsonDto implements ToJsonConvertible {

    private long systemAddress;
    private String signalName;
    private String signalNameLocalised;
    private String signalType;
    private String ussType;
    private String ussTypeLocalised;
    private String spawningState;
    private String spawningStateLocalised;
    private String spawningFaction;
    private String spawningFactionLocalised;
    private int threatLevel;
    private double timeRemaining;


    public long getSystemAddress() {
        return systemAddress;
    }

    public void setSystemAddress(long systemAddress) {
        this.systemAddress = systemAddress;
    }

    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public String getSignalNameLocalised() {
        return signalNameLocalised;
    }

    public void setSignalNameLocalised(String signalNameLocalised) {
        this.signalNameLocalised = signalNameLocalised;
    }

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }

    public String getUssType() {
        return ussType;
    }

    public void setUssType(String ussType) {
        this.ussType = ussType;
    }

    public String getUssTypeLocalised() {
        return ussTypeLocalised;
    }

    public void setUssTypeLocalised(String ussTypeLocalised) {
        this.ussTypeLocalised = ussTypeLocalised;
    }

    public String getSpawningState() {
        return spawningState;
    }

    public void setSpawningState(String spawningState) {
        this.spawningState = spawningState;
    }

    public String getSpawningStateLocalised() {
        return spawningStateLocalised;
    }

    public void setSpawningStateLocalised(String spawningStateLocalised) {
        this.spawningStateLocalised = spawningStateLocalised;
    }

    public String getSpawningFaction() {
        return spawningFaction;
    }

    public void setSpawningFaction(String spawningFaction) {
        this.spawningFaction = spawningFaction;
    }

    public String getSpawningFactionLocalised() {
        return spawningFactionLocalised;
    }

    public void setSpawningFactionLocalised(String spawningFactionLocalised) {
        this.spawningFactionLocalised = spawningFactionLocalised;
    }

    public int getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(int threatLevel) {
        this.threatLevel = threatLevel;
    }

    public double getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(double timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}

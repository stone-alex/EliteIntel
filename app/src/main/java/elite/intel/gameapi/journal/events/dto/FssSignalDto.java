package elite.intel.gameapi.journal.events.dto;

import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Objects;

public class FssSignalDto extends BaseJsonDto implements ToJsonConvertible {

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

    @Override public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        FssSignalDto that = (FssSignalDto) o;
        return getSystemAddress() == that.getSystemAddress() && getThreatLevel() == that.getThreatLevel() && Double.compare(getTimeRemaining(), that.getTimeRemaining()) == 0 && Objects.equals(getSignalName(), that.getSignalName()) && Objects.equals(getSignalNameLocalised(), that.getSignalNameLocalised()) && Objects.equals(getSignalType(), that.getSignalType()) && Objects.equals(getUssType(), that.getUssType()) && Objects.equals(getUssTypeLocalised(), that.getUssTypeLocalised()) && Objects.equals(getSpawningState(), that.getSpawningState()) && Objects.equals(getSpawningStateLocalised(), that.getSpawningStateLocalised()) && Objects.equals(getSpawningFaction(), that.getSpawningFaction()) && Objects.equals(getSpawningFactionLocalised(), that.getSpawningFactionLocalised());
    }

    @Override public int hashCode() {
        int result = Long.hashCode(getSystemAddress());
        result = 31 * result + Objects.hashCode(getSignalName());
        result = 31 * result + Objects.hashCode(getSignalNameLocalised());
        result = 31 * result + Objects.hashCode(getSignalType());
        result = 31 * result + Objects.hashCode(getUssType());
        result = 31 * result + Objects.hashCode(getUssTypeLocalised());
        result = 31 * result + Objects.hashCode(getSpawningState());
        result = 31 * result + Objects.hashCode(getSpawningStateLocalised());
        result = 31 * result + Objects.hashCode(getSpawningFaction());
        result = 31 * result + Objects.hashCode(getSpawningFactionLocalised());
        result = 31 * result + getThreatLevel();
        result = 31 * result + Double.hashCode(getTimeRemaining());
        return result;
    }
}

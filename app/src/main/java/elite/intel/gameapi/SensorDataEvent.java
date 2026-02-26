package elite.intel.gameapi;

public class SensorDataEvent {

    private final String sensorData;
    private final String instructions;

    public SensorDataEvent(String sensorData, String instructions) {
        this.instructions = instructions;
        this.sensorData = sensorData;
    }

    public String getSensorData() {
        return "sensorData: " + sensorData;
    }

    public String getInstructions() {
        return this.instructions;
    }
}

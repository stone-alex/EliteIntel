package elite.companion.session;

import elite.companion.events.FSSSignalDiscoveredEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * SystemSession
 * A singleton instance. Keeps track of the current state of the Ship AI. Stores ship sensor data.
 * Provides a consumable method to retrieve the data. Used for the Ship AI interactions. Such as reactions or storing information for internal use.
 * Consumed by Grok / Ship interactions. Not used by Voice Interactions.
 *
 */
public class SystemSession {
    public static final String SENSOR_READING = "sensor_reading";
    public static final String FSS_READING = "fss_reading";
    public static final String CURRENT_SYSTEM = "current_system";
    public static final String QUERY_DESTINATION = "query_destination";
    public static final String SHIP_DATA = "ship_data";
    public static final String LOADOUT_JSON = "loadout_json";
    private static final SystemSession INSTANCE = new SystemSession();
    private final Map<String, Object> state = new HashMap<>();
    private final Map<String, Integer> signalCounts = new HashMap<>(); // For batch accumulation
    private long currentSystemAddress = -1; // Track current system to reset on change

    private SystemSession() {
        // Private constructor to enforce a singleton pattern
    }

    public static SystemSession getInstance() {
        return INSTANCE;
    }

    public Object getObject(String key) {
        return state.get(key);
    }

    public String getSessionValue(String queryDestination, Class<String> stringClass) {
        return null;
    }

    public void remove(String key) {
        state.remove(key);
    }

    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Data from sensors: ");
        state.forEach((key, value) -> summary.append(key).append(": ").append(value).append("; "));
        return summary.toString();

    }

    public void setSensorData(String sensorReading) {
        state.put(SENSOR_READING, sensorReading);
    }

    public void updateSession(String sensorReading, Object data) {
        state.put(sensorReading, data);
    }

    public void clear() {
        state.clear();
    }

    public void clearSensorData() {
        state.remove(SENSOR_READING);
    }


    // New methods for signal batching
    public void addSignal(FSSSignalDiscoveredEvent event) {
        if (currentSystemAddress == -1) {
            currentSystemAddress = event.getSystemAddress();
        } else if (currentSystemAddress != event.getSystemAddress()) {
            // System changed mid-batch (rare), reset
            resetSignals();
            currentSystemAddress = event.getSystemAddress();
        }

        String friendlyType = getFriendlyType(event.getSignalType());
        if (friendlyType != null) signalCounts.put(friendlyType, signalCounts.getOrDefault(friendlyType, 0) + 1);
    }

    public String buildSignalSummary() {
        if (signalCounts.isEmpty()) {
            return null;
        }

        StringBuilder summary = new StringBuilder("System signals discovered: [");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : signalCounts.entrySet()) {
            if (!first) {
                summary.append(", ");
            }
            summary.append(entry.getKey()).append("s discovered: ").append(entry.getValue());
            first = false;
        }
        summary.append("] please provide brief summary of discovered signals.");
        return summary.toString();
    }

    public void resetSignals() {
        signalCounts.clear();
        currentSystemAddress = -1;
    }

    private String getFriendlyType(String signalType) {
        if (signalType == null) {
            return null;
        }
        String result;
        switch (signalType) {
            case "Outpost":
                result = "Outpost";
                break;
            case "StationBernalSphere":
                result = "Station";
                break;
            case "FleetCarrier":
                result = "Carrier";
                break;
            case "ResourceExtraction":
                result = "Extraction site";
                break;
            case "Installation":
                result = "Installation";
                break;
            default:
                result = "Other";
                break;
        }
        return result;
    }

    public void setFssData(String summary) {
        state.put(FSS_READING, summary);
    }

    public String getSensorData() {
        return state.get(SENSOR_READING) == null ? null : (String) state.get(SENSOR_READING);
    }

    public String getFssData() {
        return state.get(FSS_READING) == null ? null : (String) state.get(FSS_READING);
    }

    public void clearFssData() {
        state.remove(FSS_READING);
    }
}
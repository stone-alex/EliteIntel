package elite.companion.session;

import com.google.gson.Gson;
import elite.companion.events.BaseEvent;
import elite.companion.events.FSSSignalDiscoveredEvent;
import elite.companion.gameapi.events.NavRouteDto;

import java.util.*;

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
    public static final String SHIP_LOADOUT_JSON = "ship_loadout_json";
    public static final String SUITE_LOADOUT_JSON = "suite_loadout_json";
    public static final String DESTINATION_TARGET = "destination_target";
    public static final String FINAL_DESTINATION = "final_destination";
    public static final String CURRENT_STATUS = "current_status";
    public static final String FSD_TARGET = "fsd_target";
    private static final SystemSession INSTANCE = new SystemSession();
    private final Map<String, Object> state = new HashMap<>();
    private final Set<String> detectedSignals = new LinkedHashSet<>();
    private final Map<String, NavRouteDto> routeMap = new LinkedHashMap<>(); // Star system name to NavRouteDto
    private long currentSystemAddress = -1; // Track current system to reset on change
    private long bountyCollectedThisSession = 0;

    private SystemSession() {
        // Private constructor to enforce a singleton pattern
    }

    public static SystemSession getInstance() {
        return INSTANCE;
    }

    public Object getObject(String key) {
        return state.get(key);
    }

    public String getSessionValue(String key, Class<String> stringClass) {
        return state.get(key) == null ? null : stringClass.cast(state.get(key));
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


    public void clearSensorData() {
        state.remove(SENSOR_READING);
    }


    public void addSignal(BaseEvent event) {
        detectedSignals.add(event.toJson());
    }

    public String getSignals() {
        Object[] array = detectedSignals.stream().toArray();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(Object o : array){
            sb.append(o).append(", ");
        }
        sb.append("]");

        return array.length == 0 ? "no data" : sb.toString();
    }

    public String getSensorData() {
        return state.get(SENSOR_READING) == null ? null : (String) state.get(SENSOR_READING);
    }




    public void setNavRoute(Map<String, NavRouteDto> routeMap) {
        this.routeMap.clear();
        this.routeMap.putAll(routeMap);
    }

    public void removeNavPoint(String systemName) {
        routeMap.remove(systemName);
    }

    public Map<String, NavRouteDto> getRoute() {
        return routeMap;
    }

    public void clearRoute() {
        routeMap.clear();
    }

    public void addBounty(long totalReward) {
        bountyCollectedThisSession = bountyCollectedThisSession + totalReward;
    }

    public long getBountyCollectedThisSession() {
        return bountyCollectedThisSession;
    }

    public void clearFssSignals() {
        detectedSignals.clear();
    }
}
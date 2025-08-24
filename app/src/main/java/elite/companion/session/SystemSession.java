package elite.companion.session;

import java.util.HashMap;
import java.util.Map;

public class SystemSession {
    private static final SystemSession INSTANCE = new SystemSession();
    private final Map<String, Object> state = new HashMap<>();

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

    public void updateSession(String sensorReading, String data) {
        state.put(sensorReading, data);
    }

    public void clear() {
        state.clear();
    }
}

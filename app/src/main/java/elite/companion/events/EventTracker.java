package elite.companion.events;

import java.util.HashMap;
import java.util.Map;

public class EventTracker {

    private static Map<String, Boolean> isProcessed = new HashMap<>();

    public static boolean isProcessed(String event) {
        return isProcessed.getOrDefault(event, false);
    }

    public static void setProcessed(String event) {
        isProcessed.put(event, true);
    }

    public static void resetProcessed() {
        isProcessed.clear();
    }
}

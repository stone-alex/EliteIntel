package elite.companion.session;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.CarrierStatsEvent; // Assume this event exists

import java.util.HashMap;
import java.util.Map;

public class SessionTracker {
    public static final String CARRIER_BALANCE = "carrier_balance";
    public static final String PLAYER_STATS = "player_stats";
    private static final SessionTracker INSTANCE = new SessionTracker();
    private final Map<String, Object> state = new HashMap<>();

    public static SessionTracker getInstance() {
        return INSTANCE;
    }

    private SessionTracker() {
        EventBusManager.register(this);
        // Initialize defaults
        state.put(CARRIER_BALANCE, 0L);
        // Add more keys as needed
    }

    @Subscribe
    public void onCarrierStats(CarrierStatsEvent event) {
        state.put(CARRIER_BALANCE, event.getFinance().getCarrierBalance());
        // Update other fields
    }


    @Subscribe
    public void onPlayerStatusEvent(PlayerStats event) {
        state.put(PLAYER_STATS, event);
    }

    public String getStateSummary() {
        StringBuilder summary = new StringBuilder();
        state.forEach((key, value) -> summary.append(key).append(": ").append(value).append("; "));
        return summary.toString();
    }

    public PlayerStats getPlayerStats() {
        return state.get(PLAYER_STATS) != null ? (PlayerStats) state.get(PLAYER_STATS) : new PlayerStats();
    }
}

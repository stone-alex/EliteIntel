package elite.companion.session;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.events.CarrierStatsEvent;

import java.util.HashMap;
import java.util.Map;

public class PublicSession {
    public static final String CARRIER_BALANCE = "carrier_balance";


    private static final PublicSession INSTANCE = new PublicSession();
    public static final String CARRIER_RESERVE = "carrier_reserve";
    private final Map<String, Object> state = new HashMap<>();
    private PlayerStats playerStats;

    public static PublicSession getInstance() {
        return INSTANCE;
    }

    private PublicSession() {
        playerStats = new PlayerStats();
        EventBusManager.register(this);
    }

    @Subscribe
    public void onCarrierStats(CarrierStatsEvent event) {
        state.put(CARRIER_BALANCE, event.getFinance().getCarrierBalance());
        state.put(CARRIER_RESERVE, event.getFinance().getReserveBalance());
        state.put("carrier_fuel_level", event.getFuelLevel());
        state.put("cargo_space_used", event.getSpaceUsage());
    }


    @Subscribe
    public void onPlayerStatusEvent(PlayerStats event) {
        if (event.getPowerplayEvent() != null) {
            state.put("player_name", event.getPlayerName());
            state.put("pledged_to_power", event.getPowerplayEvent().getPower());
            state.put("pledged_to_rank", event.getPowerplayEvent().getRank());
            state.put("pledged_to_merits", event.getPowerplayEvent().getMerits());
            state.put("pledged_to_time", event.getPowerplayEvent().getTimePledged());
        }
        state.put("current_ship", event.getCurrentShip());
        state.put("current_ship_name", event.getCurrentShipName());
        if (event.getCreditBalance() > 0) state.put("personal_credits_available", event.getCreditBalance());
    }


    public void updateSession(String key, Object data) {
        state.put(key, data);
    }

    public String getStateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("You can refer to me by my either title, name or military rank. My Imperial title is: Prince, My Name is: Krondor, and my Military Rank is: Viscount. Use British cadence and spell out numerals in your replays. Round up billions to nearest million.");
        summary.append("We are loyal subjects of Her Majesty's Arissa Lavigny-Duval in service to the Empire.");
        summary.append("We are part of the imperial fleet. I own a carrier designated as LONE WOLF (plate number GHY-L8X). It is our home and base of operations. ");
        summary.append("You are an unboard AI and a member of the crew. ");
        summary.append("Currently known statistics: ");
        state.forEach((key, value) -> summary.append(key).append(": ").append(value).append("; "));
        return summary.toString();
    }


    public void storePlayerStats(PlayerStats playerStats) {
        this.playerStats = playerStats;
    }

    public PlayerStats getPlayerStats() {
        return this.playerStats;
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
}

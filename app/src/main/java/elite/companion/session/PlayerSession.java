package elite.companion.session;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.journal.events.CarrierStatsEvent;
import elite.companion.gameapi.journal.events.EventTracker;

import java.util.HashMap;
import java.util.Map;

/**
 * PlayerSession
 * A Singleton instance. Keeps track of the player state withing the current game session.
 * Provides broad context for AI interactions. Keeps information such as running player stats
 * Does not store internal ship variables or sensor data. Consumed by Grok voice interactions.
 *
 */
public class PlayerSession {
    public static final String SHIP_FUEL_LEVEL = "ship_fuel_level";
    public static final String INSURANCE_CLAIMS = "insurance_claims";
    public static final String SHIPS_OWNED = "ships_owned";
    public static final String TOTAL_BOUNTY_CLAIMED = "total_bounty_claimed";
    public static final String TOTAL_BOUNTY_PROFIT = "total_bounty_profit";
    public static final String TOTAL_DISTANCE_TRAVELED = "total_distance_traveled_in_light_years";
    public static final String TOTAL_SYSTEMS_VISITED = "total_systems_visited";
    public static final String TOTAL_HIPERSPACE_DISTANCE = "total_hyperspace_distance_in_light_years";
    public static final String TOTAL_PROFITS_FROM_EXPLORATION = "total_profits_from_exploration";
    public static final String SPECIES_FIRST_LOGGED = "species_first_logged";
    public static final String EXOBIOLOGY_PROFITS = "exobiology_profits";
    public static final String GOODS_SOLD_THIS_SESSION = "goods_sold_this_session";
    public static final String HIGHEST_SINGLE_TRANSACTION = "highest_single_transaction";
    public static final String MARKET_PROFITS = "market_profits";
    public static final String CREW_WAGS_PAYOUT = "crew_wags_payout";
    public static final String SHIP_CARGO_CAPACITY = "ship_cargo_capacity";
    public static final String SHIP_FUEL_CAPACITY = "ship_fuel_capacity";
    public static final String PLAYER_TITLE = "player_title";
    public static final String PLAYER_RANK = "player_rank";

    private static final PlayerSession INSTANCE = new PlayerSession();

    public static final String CARRIER_BALANCE = "carrier_balance";
    public static final String CARRIER_RESERVE = "carrier_reserve";
    public static final String PLAYER_NAME = "player_name";
    public static final String PLEDGED_TO_POWER = "pledged_to_power";
    public static final String POWER_RANK = "pledged_to_rank";
    public static final String MERITS = "pledged_to_merits";
    public static final String PLEDGED_DURATION = "pledged_to_time";
    public static final String CURRENT_SHIP = "current_ship";
    public static final String CURRENT_SHIP_NAME = "current_ship_name";
    public static final String CARRIER_FUEL_LEVEL = "carrier_fuel_level";
    public static final String CARGO_SPACE_USED = "cargo_space_used";
    public static final String PERSONAL_CREDITS_AVAILABLE = "personal_credits_available";
    public static final String CARRIER_CALLSIGN = "carrier_callsign";
    public static final String CARRIER_NAME = "carrier_name";
    public static final String CARRIER_TYPE = "carrier_type";
    public static final String CARRIER_DOCKING_ACCESS = "carrier_docking_access";
    public static final String CARRIER_CURRENT_JUMP_RANGE = "carrier_current_jump_range";
    public static final String CARRIER_MAX_JUMP_RANGE = "carrier_max_jump_range";
    public static final String CARRIER_ALLOWS_NOTORIOUS_ACCESS = "carrier_allows_notorious_access";
    public static final String CARRIER_PENDING_DECOMMISSION = "carrier_pending_decommission";
    public static final String CARRIER_TOTAL_CARGO_SPACE_USED = "carrier_total_cargo_space_used";
    public static final String CARRIER_CARGO_SPACE_RESERVED = "carrier_cargo_space_reserved";
    public static final String CARRIER_SHIP_PACKS = "carrier_ship_packs";
    public static final String CARRIER_MODULE_PACKS = "carrier_module_packs";
    public static final String CARRIER_FREE_SPACE = "carrier_free_space";
    public static final String CARRIER_TOTAL_CAPACITY = "carrier_total_capacity";
    public static final String CARRIER_ALLOCATED_MARKET_BALANCE = "carrier_allocated_market_balance";
    public static final String CARRIER_PIONEER_SUPPLY_TAX = "carrier_pioneer_supply_tax";
    public static final String CARRIER_SHIPYARD_SUPPLY_TAX = "carrier_shipyard_supply_tax";
    public static final String CARRIER_REARM_SUPPLY_TAX = "carrier_rearm_supply_tax";
    public static final String CARRIER_REFUEL_SUPPLY_TAX = "carrier_refuel_supply_tax";
    public static final String CARRIER_REPAIR_SUPPLY_TAX = "carrier_repair_supply_tax";


    private final Map<String, Object> state = new HashMap<>();

    public static PlayerSession getInstance() {
        return INSTANCE;
    }

    private PlayerSession() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onCarrierStats(CarrierStatsEvent event) {
        CarrierStatsEvent.Finance finance = event.getFinance();
        state.put(CARRIER_FUEL_LEVEL, event.getFuelLevel());
        state.put(CARGO_SPACE_USED, event.getSpaceUsage());
        state.put(CARRIER_CALLSIGN, event.getCallsign());
        state.put(CARRIER_NAME, event.getName());
        state.put(CARRIER_TYPE, event.getCarrierType());
        state.put(CARRIER_DOCKING_ACCESS, event.getDockingAccess());
        state.put(CARRIER_CURRENT_JUMP_RANGE, event.getJumpRangeCurr());
        state.put(CARRIER_MAX_JUMP_RANGE, event.getJumpRangeMax());
        state.put(CARRIER_ALLOWS_NOTORIOUS_ACCESS, event.isAllowNotorious());
        state.put(CARRIER_PENDING_DECOMMISSION, event.isPendingDecommission());
        if (event.getSpaceUsage() != null) {
            CarrierStatsEvent.SpaceUsage spaceUsage = event.getSpaceUsage();
            state.put(CARRIER_TOTAL_CARGO_SPACE_USED, spaceUsage.getCargo());
            state.put(CARRIER_CARGO_SPACE_RESERVED, spaceUsage.getCargoSpaceReserved());
            state.put(CARRIER_SHIP_PACKS, spaceUsage.getShipPacks());
            state.put(CARRIER_MODULE_PACKS, spaceUsage.getModulePacks());
            state.put(CARRIER_FREE_SPACE, spaceUsage.getFreeSpace());
            state.put(CARRIER_TOTAL_CAPACITY, spaceUsage.getTotalCapacity());
        }

        if (finance != null) {
            state.put(CARRIER_BALANCE, finance.getCarrierBalance());
            state.put(CARRIER_RESERVE, finance.getReserveBalance());
            state.put(CARRIER_ALLOCATED_MARKET_BALANCE, finance.getAvailableBalance());
            state.put(CARRIER_PIONEER_SUPPLY_TAX, finance.getTaxRate_pioneersupplies());
            state.put(CARRIER_SHIPYARD_SUPPLY_TAX, finance.getTaxRate_shipyard());
            state.put(CARRIER_REARM_SUPPLY_TAX, finance.getTaxRate_rearm());
            state.put(CARRIER_REFUEL_SUPPLY_TAX, finance.getTaxRate_refuel());
            state.put(CARRIER_REPAIR_SUPPLY_TAX, finance.getTaxRate_repair());

            long carrierBalance = finance.getCarrierBalance();
            long reserveBalance = finance.getReserveBalance();
            updateSession(PlayerSession.CARRIER_BALANCE, String.valueOf(carrierBalance));
            updateSession(PlayerSession.CARRIER_RESERVE, String.valueOf(reserveBalance));

            if (!EventTracker.isProcessed(event.getEventName())) {
                int jumps = event.getFuelLevel() / 90;
                updateSession("carrier_stats", "Credit balance: " + carrierBalance + " reserved balance: " + reserveBalance + " fuel level: " + event.getFuelLevel() + " enough for " + event.getFuelLevel() / 90 + " jumps or " + (jumps * 500) + " light years");
            }
        }
    }

    public void updateSession(String key, Object data) {
        state.put(key, data);
    }

    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<String, Object> entry : state.entrySet()) {
            String key = entry.getKey();
            Object value = String.valueOf(entry.getValue());
            summary.append(key).append(": ").append(String.valueOf(value)).append("; ");
        }
        return summary.toString();
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

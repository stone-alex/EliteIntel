package elite.companion.comms.handlers.query;

public enum QueryActions {
    CHECK_LEGAL_STATUS("check_legal_status", false), // Data
    LIST_AVAILABLE_VOICES("list_available_voices", true), // Quick
    LOCAL_SYSTEM_INFO("local_system_info", false), // Data
    QUERY_ANALYZE_ON_BOARD_CARGO("query_analyze_on_board_cargo", false), // Data
    QUERY_ANALYZE_ROUTE("query_analyze_route", false), // Data
    QUERY_CARRIER_STATS("query_carrier_stats", false), // Data
    QUERY_FIND_NEAREST_MATERIAL_TRADER("query_find_nearest_material_trader", false), // Data
    QUERY_NEXT_STAR_SCOOPABLE("query_next_star_scoopable", false), // Data
    QUERY_PIRATE_KILLS_REMAINING("query_pirate_kills_remaining", false), // Data
    QUERY_PIRATE_MISSION_PROFIT("query_pirate_mission_profit", false),//Data
    QUERY_PIRATE_STATUS("query_pirate_status", false), // Data
    QUERY_PLAYER_STATS_ANALYSIS("query_player_stats_analysis", false), // Data
    QUERY_SEARCH_LOCAL_SIGNALS_DATA("query_search_local_signals_data", false), // Data
    QUERY_SEARCH_SIGNAL_DATA("query_search_signal_data", false), // Data
    QUERY_SHIP_LOADOUT("query_ship_loadout", false), // Data
    WHAT_ARE_YOUR_CAPABILITIES("what_are_your_capabilities", false), // Data (if analysis delay; adjust if static)
    WHAT_IS_YOUR_DESIGNATION("what_is_your_designation", true) // Quick
    ;

    private final String action;
    private final boolean isQuick;

    QueryActions(String action, boolean isQuick) {
        this.action = action;
        this.isQuick = isQuick;
    }

    public String getAction() {
        return action;
    }

    public boolean isQuick() {
        return isQuick;
    }
}
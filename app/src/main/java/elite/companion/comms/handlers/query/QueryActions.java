package elite.companion.comms.handlers.query;

public enum QueryActions {
    //Queries
    QUERY_SEARCH_SIGNAL_DATA("query_search_local_signals_data", null, null),
    QUERY_SHIP_LOADOUT("query_ship_loadout", null, null),
    QUERY_ANALYZE_ROUTE("query_analyze_route", null, null),
    QUERY_ANALYZE_ON_BOARD_CARGO("query_analyze_onboard_cargo_manifest", null, null),
    QUERY_FIND_NEAREST_MATERIAL_TRADER("query_find_nearest_material_trader", null, null),
    LIST_AVAILABLE_VOICES("list_available_voices", null, null),
    LOCAL_SYSTEM_INFO("access_local_system_data_and_tell_me_what_you_know", null, null),
    CHECK_LEGAL_STATUS("check_legal_status", null, null),
    QUERY_PIRATE_KILLS_REMAINING("query_pirate_massacre_kills_remaining", null, null),
    QUERY_PIRATE_MISSION_PROFIT("query_pirate_massacre_mission_profit", null, null),
    QUERY_PIRATE_STATUS("query_pirate_massacre_status", null, null),
    QUERY_NEXT_STAR_SCOOPABLE("query_next_star_fuel_scoopable", null, null),
    QUERY_CARRIER_STATS("query_fleet_carrier_stats", null, null),
    QUERY_TELL_ME_YOUR_NAME("query_what_is_your_designation", null, null),


    ; //<-- end of enum

    private final String action;
    private final String placeholder;
    private final String paramKey;

    QueryActions(String action, String placeholder, String paramKey) {
        this.action = action;
        this.placeholder = placeholder;
        this.paramKey = paramKey;
    }

    public String getAction() {
        return action;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getParamKey() {
        return paramKey;
    }

    public String getCommandWithPlaceholder() {
        return placeholder != null ? action + " " + placeholder : action;
    }
}

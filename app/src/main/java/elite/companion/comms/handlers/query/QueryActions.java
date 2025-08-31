package elite.companion.comms.handlers.query;

public enum QueryActions {
    CHECK_LEGAL_STATUS("check_legal_status", AnalyzeDataHandler.class, false),
    LOCAL_SYSTEM_INFO("local_system_info", AnalyzeDataHandler.class, false),
    QUERY_ANALYZE_ON_BOARD_CARGO("query_analyze_on_board_cargo", AnalyzeDataHandler.class, false),
    QUERY_ANALYZE_ROUTE("query_analyze_route", AnalyzeDataHandler.class, false),
    QUERY_SEARCH_SIGNAL_DATA("query_search_signal_data", AnalyzeDataHandler.class, false),
    QUERY_SHIP_LOADOUT("query_ship_loadout", AnalyzeDataHandler.class, false),
    QUERY_NEXT_STAR_SCOOPABLE("query_next_star_scoopable", AnalyzeDataHandler.class, false),
    QUERY_CARRIER_STATS("query_carrier_stats", AnalyzeDataHandler.class, false),
    QUERY_PIRATE_KILLS_REMAINING("query_pirate_kills_remaining", PirateMissionAnalyzer.class, true),
    QUERY_PIRATE_MISSION_PROFIT("query_pirate_mission_profit", PirateMissionAnalyzer.class, true),
    QUERY_PIRATE_STATUS("query_pirate_status", PirateMissionAnalyzer.class, true),
    LIST_AVAILABLE_VOICES("list_available_voices", ListAvailableVoices.class, false),
    WHAT_IS_YOUR_DESIGNATION("what_is_your_designation", WhatIsYourNameHandler.class, false),
    WHAT_ARE_YOUR_CAPABILITIES("what_are_your_capabilities", WhatAreYourCapabilitiesHandler.class, false),
    QUERY_PLAYER_STATS_ANALYSIS("query_player_stats_analysis", PlayerStatsAnalyzer.class, true),
    ;

    private final String action;
    private final Class<? extends QueryHandler> handlerClass;
    private final boolean requiresFollowUp;

    QueryActions(String action, Class<? extends QueryHandler> handlerClass, boolean requiresFollowUp) {
        this.action = action;
        this.handlerClass = handlerClass;
        this.requiresFollowUp = requiresFollowUp;
    }

    public String getAction() {
        return action;
    }

    public Class<? extends QueryHandler> getHandlerClass() {
        return handlerClass;
    }

    public boolean isRequiresFollowUp() {
        return requiresFollowUp;
    }
}
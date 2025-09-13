package elite.companion.ai.brain.handlers.query;

public enum QueryActions {
    ANALYZE_FSD_TARGET("query_analyze_fsd_target", "Analyze FSD target data and provide summary for allegiance, traffic and security. FSD target is the destination.", AnalyzeFsdTargetHandler.class, false),
    ANALYZE_LOCAL_MARKETS("query_analyze_market", "Analyze  local market contents.", AnalyzeLocalMarketsHandler.class, false),
    ANALYZE_LOCAL_OUTFITTING("query_analyze_outfitting", "Analyze outfitting options. ", AnalyzeLocalOutfittingHandler.class, false),
    ANALYZE_LOCAL_SHIPYARD("query_shipyard_data", "Analyse shipyard contents. ", AnalyzeShipyardHandler.class, false),
    ANALYZE_SCAN("query_analyze_last_scan", "Provide analysis of this scan data of the stellar object. ", AnalyzerStellarObjectHandler.class, false),
    ANALYZE_STELLAR_OBJECTS("query_analyze_local_stellar_objects", "Analyze current star system and nearby stellar objects. Gravity unit G, Temperature unit C ", AnalyzeStellarObjectsHandler.class, false),
    ANALYZE_BODY_MATERIALS("query_planet_materials_for_harvest", "Analyze materials available. ", ProvideSummaryOfMaterialsOnPlanet.class, false),
    ANALYZE_CURRENT_STATUS("check_current_status", "Data may contain legal status, cargo and credit balance. ", AnalyzeDataHandler.class, false),
    FIND_MATERIAL_TRADER("find_material_trader", "Find us a material trader. ", MaterialTraderQueryHandler.class, false),
    GENERAL_CONVERSATION("general_conversation", "Handles general knowledge questions outside Elite Dangerous (e.g., 'what's the weather in Los Angeles?'). ", ConversationalQueryHandler.class, true),
    HOW_FAR_TO_FINAL_DESTINATION("how_far_to_final_destination", "Run distance to final destination analysis", AnalyzeDistanceToFinalDestination.class, false),
    LIST_AVAILABLE_VOICES("list_available_voices", "Lists available AI voices. ", ListAvailableVoices.class, false),
    LOCAL_SYSTEM_INFO("local_system_info", "Provide information about local star system. ", AnalyzeLocalSystemHandler.class, false),
    QUERY_ANALYZE_ON_BOARD_CARGO("query_analyze_on_board_cargo", "Analyzes cargo hold contents. ", AnalyzeDataHandler.class, false),
    QUERY_ANALYZE_ROUTE("query_analyze_route", "Analyze our plotted route. ", AnalyzeRouterHandler.class, false),
    QUERY_CARRIER_STATS("query_carrier_stats", "Summarize carrier information. ", AnalyzeDataHandler.class, false),
    QUERY_PIRATE_MISSION_KILLS_REMAINING("query_pirate_mission_kills_remaining", "Summarize the state of the missions. Mission kills stack across factions ", PirateMissionAnalyzer.class, true),
    QUERY_PIRATE_MISSION_PROFIT("query_pirate_mission_profit", "Summarize the potential profit from currently active missions. ", PirateMissionAnalyzer.class, true),
    QUERY_PIRATE_MISSION_STATUS("query_pirate_mission_status", "Handles summarize current progression of missions. ", PirateMissionAnalyzer.class, true),
    QUERY_NEXT_STAR_SCOOPABLE("query_next_star_scoopable", "Check if we can get fuel at the next star. ", AnalyzeDataHandler.class, false),
    QUERY_PLAYER_STATS_ANALYSIS("query_player_stats_analysis", "Analyze current player statistics. ", PlayerStatsAnalyzer.class, true),
    QUERY_SEARCH_SIGNAL_DATA("query_local_info", "Analyze information for our current location ", AnalyzeDataHandler.class, false),
    QUERY_SHIP_LOADOUT("query_ship_loadout", "Requests current ship details (e.g., 'what is my ship loadout'). ", AnalyzeDataHandler.class, false),
    STATION_DATA("query_station_data", "Access station data, provide analysis. ", StationDataHandler.class, false),
    TRIVIA("trivia", "General conversion question, use your own knowledge to respond. ", TriviaQueryHandler.class, true),
    WHAT_ARE_YOUR_CAPABILITIES("what_are_your_capabilities", "Summarizes app capabilities. ", WhatAreYourCapabilitiesHandler.class, false),
    WHAT_IS_YOUR_DESIGNATION("what_is_your_designation", "Responds with AI name (e.g., 'what is your name', 'who are you'). ", WhatIsYourNameHandler.class, false),
    WHERE_IS_OUR_CARRIER("what_is_our_fleet_carrier_location", "Check on our carrier current location. ", WhatIsOurCarrierLocationHandler.class, false),
    TOTAL_BOUNTIES_COLLECTED("query_total_bounties_collected", "Summ total of the bounties collected this sessiosn", AnalyzeBountiesCollectedHandler.class, false);

    private final String action;
    private final String description;
    private final Class<? extends QueryHandler> handlerClass;
    private final boolean requiresFollowUp;

    QueryActions(String action, String description, Class<? extends QueryHandler> handlerClass, boolean requiresFollowUp) {
        this.action = action;
        this.description = description;
        this.handlerClass = handlerClass;
        this.requiresFollowUp = requiresFollowUp;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public Class<? extends QueryHandler> getHandlerClass() {
        return handlerClass;
    }

    public boolean isRequiresFollowUp() {
        return requiresFollowUp;
    }
}
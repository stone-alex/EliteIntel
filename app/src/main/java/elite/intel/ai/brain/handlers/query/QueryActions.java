package elite.intel.ai.brain.handlers.query;

public enum QueryActions {
    ANALYZE_FSD_TARGET("query_analyze_fsd_target", "Analyze FSD target data and provide summary for allegiance, traffic and security. FSD target is the destination.", AnalyzeFsdTargetHandler.class, true),
    ANALYZE_LOCAL_MARKETS("query_analyze_market", "Analyze  local market contents.", AnalyzeLocalMarketsHandler.class, true),
    ANALYZE_LOCAL_OUTFITTING("query_analyze_outfitting", "Analyze outfitting options. ", AnalyzeLocalOutfittingHandler.class, true),
    ANALYZE_LOCAL_SHIPYARD("query_shipyard_data", "Analyse shipyard contents. ", AnalyzeShipyardHandler.class, true),
    ANALYZE_SCAN("query_analyze_last_scan", "Provide analysis of this scan data of the stellar object. ", AnalyzeLastScanHandler.class, true),
    ANALYZE_STELLAR_OBJECTS("query_analyze_local_stellar_objects", "Analyze current star system and nearby stellar objects. Gravity unit G, Temperature unit C ", AnalyzeStellarObjectsHandler.class, true),
    ANALYZE_STELLAR_OBJECT("query_analyze_this_planet", "Provide analysis of stellar body", AnalyzerStellarObjectHandler.class, true),
    ANALYZE_BODY_MATERIALS("query_planet_materials_for_harvest", "Analyze materials available. ", AnalyzeMaterialsOnPlanetHandler.class, true),
    ANALYZE_CURRENT_PLANET("query_planet_data_analysis", "Answer questions based on data for the current planet", AnalyzeCurrentPlanetHandler.class, true),
    ANALYZE_CURRENT_FUEL_STATUS("query_fuel_status", "Analyze fuel tank capacity in shipLoadout and fuelData available reserve, provide result as percent fuel available", AnalyzeFuelStatusHandler.class, true),
    ANALYZE_LOCAL_STATIONS("query_local_stations", "Provide summary a service the local stations if any", AnalyzeLocalStations.class, true),
    HOW_FAR_TO_FINAL_DESTINATION("how_far_to_final_destination", "Run distance to final destination analysis", AnalyzeDistanceToFinalDestination.class, false),
    LIST_AVAILABLE_VOICES("list_available_voices", "Lists available AI voices. ", ListAvailableVoices.class, false),
    LOCAL_SYSTEM_INFO("local_system_info", "Provide information about local star system. ", AnalyzeLocalSystemHandler.class, true),
    QUERY_ANALYZE_ON_BOARD_CARGO("query_analyze_on_board_cargo", "Analyzes cargo hold contents. ", AnalyzeCargoHoldHandler.class, true),
    QUERY_ANALYZE_ROUTE("query_analyze_route", "Analyze our plotted route. ", AnalyzeRouterHandler.class, true),
    QUERY_CARRIER_STATS("query_our_fleet_carrier_stats", "Analyse our fleet carrier data, provide extremely short and concise answer for specific datapoint only, if not specified provide summary", AnalyzeCarrierDataHandler.class, true),
    QUERY_PIRATE_MISSION_KILLS_REMAINING("query_pirate_mission_kills_remaining", "Summarize the state of the missions. Mission kills stack across factions ", AnalyzePirateMissionHandler.class, true),
    QUERY_PIRATE_MISSION_PROFIT("query_pirate_mission_profit", "Summarize the potential profit from currently active missions. ", AnalyzePirateMissionHandler.class, true),
    QUERY_PIRATE_MISSION_STATUS("query_pirate_mission_status", "Handles summarize current progression of missions. ", AnalyzePirateMissionHandler.class, true),
    QUERY_NEXT_STAR_SCOOPABLE("query_next_star_scoopable", "Check if we can get fuel at the next star. ", AnalyzeNextStarForFuelHandler.class, false),
    QUERY_PLAYER_STATS_ANALYSIS("query_player_stats_analysis", "Analyze current player statistics. ", PlayerStatsAnalyzer.class, true),
    QUERY_SEARCH_SIGNAL_DATA("query_signal_local_info", "Analyze information for our current location ", AnalyzeSignalDataHandler.class, true),
    QUERY_SHIP_LOADOUT("query_ship_loadout", "Provide requested information about ship loadout", AnalyzeShipLoadoutHandler.class, true),
    STATION_DATA("query_station_data", "Access station data, provide analysis. ", StationDataHandler.class, true),
    WHAT_ARE_YOUR_CAPABILITIES("what_are_your_capabilities", "Summarizes app capabilities. ", WhatAreYourCapabilitiesHandler.class, false),
    WHAT_IS_YOUR_DESIGNATION("what_is_your_designation", "Responds with AI name (e.g., 'what is your name', 'who are you'). ", WhatIsYourNameHandler.class, false),
    WHERE_IS_OUR_CARRIER("what_is_our_fleet_carrier_location", "Check on our carrier current location. ", WhatIsOurCarrierLocationHandler.class, false),
    TOTAL_BOUNTIES_COLLECTED("query_total_bounties_collected", "Summ total of the bounties collected this session", AnalyzeBountiesCollectedHandler.class, false),
    GENERAL_CONVERSATION("general_conversation", "General conversation, use your own knowledge to respond", ConversationalQueryHandler.class, true);

    // NOT implemented yet
    //FIND_MATERIAL_TRADER("find_material_trader", "Find us a material trader. ", MaterialTraderQueryHandler.class, true);

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
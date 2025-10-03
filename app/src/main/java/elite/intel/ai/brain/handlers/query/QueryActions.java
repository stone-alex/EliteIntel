package elite.intel.ai.brain.handlers.query;

public enum QueryActions {

    ANALYZE_SCAN("query_analyze_last_scan", "Analyze the most recent scan data.", AnalyzeLastScanHandler.class, true),
    QUERY_SEARCH_SIGNAL_DATA("query_planetary_moon_signals", "Analyze data from planets or moons in the star system, including materials and bio signals using gravity (G) and temperature (K) units.", AnalyzeSignalDataHandler.class, true),  // Clarify planetary/moon scope, avoid vehicle overlap
    ANALYZE_CURRENT_PLANET("query_current__location", "Where are we? Analyze data for the current planetary or station location.", AnalyzeCurrentLocationHandler.class, true),  // Emphasize planetary/station context
    ANALYZE_STAR_SYSTEM_EXPLORATION("query_star_system_exploration", "Analyze exploration data and profits for the current star system.", AnalyzeExplorationProfitsHandler.class, true),
    ANALYZE_BODY_MATERIALS("query_planetary_materials", "Analyze material composition on this planet.", AnalyzeMaterialsOnPlanetHandler.class, true),  // Specify planetary to avoid ship/cargo confusion
    ANALYZE_EXO_BIOLOGY("query_exobiology_samples", "Analyze bio-sample collection progress, including completed, partial, and remaining species.", AnalyzeBioSamplesHandler.class, true),  // "exobiology" to distinguish from commodities
    ANALYZE_CURRENT_FUEL_STATUS("query_ship_fuel_status", "Analyze ship fuel tank capacity and reserve, return percent available.", AnalyzeFuelStatusHandler.class, true),
    ANALYZE_FSD_TARGET("query_fsd_target_analysis", "Analyze FSD destination for allegiance, traffic, and security.", AnalyzeFsdTargetHandler.class, true),
    ANALYZE_LOCAL_MARKETS("query_local_commodity_markets", "Analyze commodity market contents at the current location.", AnalyzeLocalMarketsHandler.class, true),  // "commodity" to avoid bio or material overlap
    ANALYZE_LOCAL_OUTFITTING("query_local_outfitting", "Analyze available outfitting options.", AnalyzeLocalOutfittingHandler.class, true),
    ANALYZE_LOCAL_SHIPYARD("query_local_shipyard", "Analyze shipyard contents.", AnalyzeShipyardHandler.class, true),
    ANALYZE_LOCAL_STATIONS("query_local_stations_services", "Summarize services at local stations.", AnalyzeLocalStations.class, true),
    HOW_FAR_TO_FINAL_DESTINATION("query_distance_to_final_destination", "Calculate distance to the final destination.", AnalyzeDistanceToFinalDestination.class, false),
    LIST_AVAILABLE_VOICES("list_available_voices", "List available AI voices.", ListAvailableVoices.class, false),
    LOCAL_SYSTEM_INFO("query_local_star_system", "Provide details about the current star system.", AnalyzeLocalSystemHandler.class, true),
    QUERY_ANALYZE_ON_BOARD_CARGO("query_ship_cargo_contents", "Analyze contents of the ship’s cargo hold.", AnalyzeCargoHoldHandler.class, true),  // "ship" to avoid carrier/planet mix-up
    QUERY_ANALYZE_ROUTE("query_plotted_route_analysis", "Analyze the current plotted route.", AnalyzeRouterHandler.class, true),
    QUERY_CARRIER_STATS("query_fleet_carrier_vehicle_stats", "Analyze fleet carrier vehicle data (e.g., fuel, market, location), provide concise answer for specific datapoints or a summary if unspecified.", AnalyzeCarrierDataHandler.class, true),  // "vehicle" to distinguish from planets
    QUERY_PIRATE_MISSION_KILLS_REMAINING("query_pirate_mission_kills", "Summarize remaining kills for active pirate missions.", AnalyzePirateMissionHandler.class, true),
    QUERY_PIRATE_MISSION_PROFIT("query_pirate_mission_profit", "Summarize potential profit from active pirate missions.", AnalyzePirateMissionHandler.class, true),
    QUERY_PIRATE_MISSION_STATUS("query_pirate_mission_progress", "Summarize progress of active pirate missions.", AnalyzePirateMissionHandler.class, true),
    QUERY_NEXT_STAR_SCOOPABLE("query_next_star_fuel", "Check if the next star is scoopable for fuel.", AnalyzeNextStarForFuelHandler.class, false),
    QUERY_PLAYER_STATS_ANALYSIS("query_player_statistics", "Analyze current player statistics.", PlayerStatsAnalyzer.class, true),
    QUERY_SHIP_LOADOUT("query_ship_loadout_details", "Provide details about the ship’s loadout.", AnalyzeShipLoadoutHandler.class, true),
    STATION_DATA("query_station_details", "Analyze data for the current station.", StationDataHandler.class, true),
    WHAT_ARE_YOUR_CAPABILITIES("query_app_capabilities", "Summarize application capabilities.", WhatAreYourCapabilitiesHandler.class, false),
    WHAT_IS_YOUR_DESIGNATION("query_ai_designation", "Respond with the AI’s name or designation.", WhatIsYourNameHandler.class, false),
    WHERE_IS_OUR_CARRIER("query_fleet_carrier_location", "Provide the current location of our fleet carrier. Do not confuse with question about distance to the carrier.", WhatIsOurCarrierLocationHandler.class, false),
    TOTAL_BOUNTIES_COLLECTED("query_bounties_collected", "Summarize total bounties collected this session.", AnalyzeBountiesCollectedHandler.class, false),
    CARRIER_ETA("query_fleet_carrier_arrival_eta", "Calculate fleet carrier ETA using arrival and current time.", CarrierETAHandler.class, false),
    HOW_FAR_ARE_WE_FROM_BUBBLE("query_distance_to_bubble", "Calculate distance to the Bubble in light years using 3D coordinates.", AnalyzeDistanceFromTheBubble.class, false),
    HOW_FAR_IS_OUR_CARRIER("query_distance_to_fleet_carrier", "Calculate distance to our fleet carrier in light years using 3D coordinates.", AnalyzeDistanceFromFleetCarrierHandler.class, false),
    HOW_FAR_ARE_WE_FROM_LAST_BIO_SAMPLE("query_distance_to_last_exobiology_sample", "Calculate distance to the last bio-sample using user latitude, longitude, planet radius, and sample coordinates.", AnalyzeDistanceFromLastBioSample.class, true),  // "exobiology" for clarity
    GENERAL_CONVERSATION("general_conversation", "Handle general conversation when no other query matches.", ConversationalQueryHandler.class, true);


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
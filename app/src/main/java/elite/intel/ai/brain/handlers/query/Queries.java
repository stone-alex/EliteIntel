package elite.intel.ai.brain.handlers.query;

import elite.intel.ai.brain.handlers.query.struct.AnalyseMaterialsHandler;

public enum Queries {

    HELP("help_with_topic", "--", HelpHandler.class, true),
    ANALYZE_KEY_BINDINGS("query_analyze_key_bindings",
            "Use this data to assist the user in correcting or informing about missing key bindings." +
                    "Reply with a specific item from the list." +
                    "If the user asks for 'next binding', reply with the first one in the list." +
                    "If the user asks for similar bindings, list all related bindings. Like: 'Map', 'Buggy','UI', 'HUD' etc..",
            AnalyzeMisingKeyBindingHandler.class,
            true),

    ANALYZE_SYSTEM_SECURITY("query_analyze_system_security", "--", AnalyzeSystemSecurityHandler.class, true),
    ANALYZE_TRADE_PROFILE("query_current_trade_profile_info", "--", AnalyzeTradeProfileHandler.class, true),
    ANALYZE_DISTANCE_TO_STELLAR_OBJECT("query_what_is_the_distance_to_planet", "-- instructions in class --", AnalyzeDistanceToStellarObject.class, true),
    ANALYZE_SCAN("query_analyze_last_scan", "Analyze the most recent scan data.", AnalyzeLastScanHandler.class, true),
    QUERY_SEARCH_SIGNAL_DATA("query_star_system_data_signals_bio_samples_for_vista_genomics_stations_and_planetary_stats", "Data may contain information about planets, moons, planetary rings, raw materials, detectedSignals, bio forms, bio samples, stations, starports etc. Use gravity (G) and temperature (K) units. Offer celsius alternative. Use allCompletedBioScans for questions about bio samples not yet delivered to Vista Genomics.  Use planetShotName for planets.  If asked about how long the day last, answer in hours and minutes", AnalyzeSignalDataHandler.class, true),

    ANALYZE_MATERIALS_ON_HAND("query_analyze_storage_for_materials", "Provide answers about materials on hand based on this data. Use maxCap data field to compare amount available to cap amount. Material amount is measured in units. Example Answer: 'We have 12 units of mercury out of 200'", AnalyseMaterialsHandler.class, true),
    ANALYZE_STAR_SYSTEM_EXPLORATION("query_exploration_profits_data", "Use this data to provide answers on potential exo-biology exploration profits.", AnalyzeExplorationProfitsHandler.class, true),
    ANALYZE_CURRENT_PLANET("query_current_location", "Use this data to provide answers for our location. NOTE: For questions such as 'where are we?' Use planetShortName for location name unless we are on the station. If we are on a station, return station name and planet we are orbiting.", AnalyzeCurrentLocationHandler.class, true),  // Emphasize planetary/station context,
    ANALYZE_BODY_MATERIALS("query_planetary_materials_present_on_planet", "Analyze material composition on this planet.", AnalyzeMaterialsOnPlanetHandler.class, true),  // Specify planetary to avoid ship/cargo confusion,
    ANALYZE_EXO_BIOLOGY("query_organic_and_exobiology_data_for_current_planet", "-/-", AnalyzeBioSamplesHandler.class, true),  // "exobiology" to distinguish from commodities,

    ANALYZE_CURRENT_FUEL_STATUS("query_ship_fuel_status", "Analyze ship fuel tank capacity and reserve, return percent available. Do not confuse ship with fleet carrier. Fleet carrier uses tritium for fuel, Ship uses hydrogen.", AnalyzeFuelStatusHandler.class, true),
    ANALYZE_FSD_TARGET("query_analyze_fsd_target", "Analyze selected FTL destination for allegiance, traffic, and security.", AnalyzeFsdTargetHandler.class, true),
    ANALYZE_TRADE_ROUTE("query_trade_route_analysis", "Analyze trade route for current location.", AnalyzeTradeRouteHandler.class, true),

    ANALYZE_LOCAL_OUTFITTING("query_local_outfitting", "Analyze available outfitting options.", AnalyzeLocalOutfittingHandler.class, true),
    ANALYZE_LOCAL_SHIPYARD("query_local_shipyard", "Analyze shipyard contents.", AnalyzeShipyardHandler.class, true),
    ANALYZE_LOCAL_STATIONS("query_local_stations_services", "Summarize services at local stations.", AnalyzeLocalStations.class, true),
    HOW_FAR_TO_FINAL_DESTINATION("query_ship_distance_to_final_destination", "Calculate distance to the final destination.", AnalyzeDistanceToFinalDestination.class, false),
    LIST_AVAILABLE_VOICES("list_available_voices", "List available AI voices.", ListAvailableVoices.class, false),
    QUERY_ANALYZE_ON_BOARD_CARGO("query_ship_cargo_contents_for_commodities", "Use this data provide questions regarding cargo and/or ship loadout if relevant. Cargo is listed 1 unit = 1 ton. Do not ask follow up questions. Just provide information.", AnalyzeCargoHoldHandler.class, true),  // "ship" to avoid carrier/planet mix-up,
    QUERY_ANALYZE_ROUTE("query_ship_plotted_route_analysis", "Analyze the current plotted route. Number of  jumps = number of nodes.", AnalyzeRouterHandler.class, true),
    QUERY_ANALYZE_FUEL_ROUTE("query_can_we_fuel_on_route_analysis", "Analyze this data for re-fuel potential", AnalyzeRouterHandler.class, true),

    ANALYZE_CARRIER_ROUTE("query_fleet_carrier_route", "Use this data to analyze the current fleet carrier route.", AnalyzeCarrierRouteHandler.class, true),
    ANALYZE_FLEET_CARRIER_FUEL_SUPPLY("query_fleet_carrier_tritium_fuel_supply", "Analyze fleet carrier fuel supply.", AnalyzeFleetCarrierFuelSupplyHandler.class, true),
    WHAT_IS_OUR_CARRIER_DESTINATION("query_what_is_our_fleet_carrier_destination", "Return name of the star system the fleet carrier is headed to. Or no carrier route set, if empty", AnalyzeFleetCarrierFinalDestinationHandler.class, true),
    QUERY_CARRIER_STATS("query_fleet_carrier_info_range_fuel_etc", "Analyze fleet carrier data (e.g., fuel, market, balance, location)", AnalyzeCarrierDataHandler.class, true),  // "vehicle" to distinguish from planets
    CARRIER_ETA("query_fleet_carrier_arrival_eta", "Calculate fleet carrier ETA using arrival and current time.", CarrierETAHandler.class, false),
    HOW_FAR_IS_OUR_CARRIER("query_distance_to_fleet_carrier", "Calculate distance to our fleet carrier in light years using 3D coordinates.", AnalyzeDistanceFromFleetCarrierHandler.class, false),

    /// First query might be enough. TODO: Check.
    QUERY_MISSIONS("query_summarize_outstanding_missions", "Provide summary of outstanding missions", AnalyzePirateMissionHandler.class, true),
    QUERY_PIRATE_MISSION_KILLS_REMAINING("query_pirate_mission_kills", "Summarize remaining kills for active pirate missions.", AnalyzePirateMissionHandler.class, true),
    QUERY_PIRATE_MISSION_PROFIT("query_analyze_pirate_mission_profit", "Summarize potential profit from active pirate missions.", AnalyzePirateMissionHandler.class, true),
    QUERY_PIRATE_MISSION_STATUS("query_analyze_pirate_mission_progress", "Summarize progress of active pirate missions.", AnalyzePirateMissionHandler.class, true),

    QUERY_NEXT_STAR_SCOOPABLE("query_next_star_fuel", "Check if the next star is scoopable for fuel.", AnalyzeNextStarForFuelHandler.class, false),
    QUERY_PLAYER_STATS_ANALYSIS("query_analyze_player_profile", "Summarize player statistics.", AnalyzePlayerProfile.class, true),
    QUERY_SHIP_LOADOUT("query_ship_loadout_details", "Use this data to answer user queries about ship loadout details.", AnalyzeShipLoadoutHandler.class, true),
    STATION_DATA("query_station_details", "Analyze data for the current station.", StationDataHandler.class, true),
    WHAT_ARE_YOUR_CAPABILITIES("query_app_capabilities", "Summarize application capabilities.", WhatAreYourCapabilitiesHandler.class, false),
    WHAT_IS_YOUR_DESIGNATION("query_ai_designation", "Respond with the AI’s name or designation.", WhatIsYourNameHandler.class, false),
    TOTAL_BOUNTIES_COLLECTED("query_bounties_collected", "Summarize total bounties collected this session.", AnalyzeBountiesCollectedHandler.class, false),
    HOW_FAR_ARE_WE_FROM_BUBBLE("query_distance_to_bubble", "Calculate distance to the Bubble in light years using 3D coordinates.", AnalyzeDistanceFromTheBubble.class, false),
    HOW_FAR_ARE_WE_FROM_LAST_BIO_SAMPLE("query_distance_to_last_exobiology_sample", "Calculate distance to the last bio-sample using user latitude, longitude, planet radius, and sample coordinates.", AnalyzeDistanceFromLastBioSample.class, true),
    TIME_IN_ZONE("what_time_is_it_in", "Calculate current time at the location requested by user. The data provides current UTC time", TimeQueryHandler.class, true),
    PERFORM_PRELIMINARY_BIOME_ANALYSIS("query_analyse_biome_for_planet_or_planets", "Use data provided to analyze probable genus and species that might be present on the plant(s)", PlanetBiomeAnalyzerHandler.class, true),

    REMINDER("query_remind_me", "use the data provided to remind the user what where we going and what we are doing", RemindTargetDestinationHandler.class, false),
    GENERAL_CONVERSATION("general_conversation", "Handle general conversation when no other query matches.", ConversationalQueryHandler.class, false);


    private final String action;
    private final String instructions;
    private final Class<? extends QueryHandler> handlerClass;
    private final boolean requiresFollowUp;

    Queries(String action, String description, Class<? extends QueryHandler> handlerClass, boolean requiresFollowUp) {
        this.action = action;
        this.instructions = description;
        this.handlerClass = handlerClass;
        this.requiresFollowUp = requiresFollowUp;
    }

    public String getAction() {
        return action;
    }

    public String getInstructions() {
        return instructions;
    }

    public Class<? extends QueryHandler> getHandlerClass() {
        return handlerClass;
    }

    public boolean isRequiresFollowUp() {
        return requiresFollowUp;
    }
}
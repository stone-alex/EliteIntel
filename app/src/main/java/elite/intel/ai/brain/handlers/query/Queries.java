package elite.intel.ai.brain.handlers.query;

import elite.intel.ai.brain.handlers.query.struct.AnalyseMaterialsHandler;

public enum Queries {

    HELP("help_with_topic",  HelpHandler.class, true),
    ANALYZE_KEY_BINDINGS("query_analyze_key_bindings",  AnalyzeMisingKeyBindingHandler.class, true),

    ANALYZE_SYSTEM_SECURITY("query_analyze_system_security",  AnalyzeSystemSecurityHandler.class, true),
    ANALYZE_TRADE_PROFILE("query_current_trade_profile_info",  AnalyzeTradeProfileHandler.class, true),
    ANALYZE_DISTANCE_TO_STELLAR_OBJECT("query_what_is_the_distance_to_planet",  AnalyzeDistanceToStellarObject.class, true),
    ANALYZE_SCAN("query_analyze_last_scan",  AnalyzeLastScanHandler.class, true),

    /// split in ot several queries
    QUERY_SEARCH_SIGNAL_DATA("query_star_system_data_signals_bio_samples_for_vista_genomics_stations_and_planetary_stats",  AnalyzeSignalDataHandler.class, true),

    ANALYZE_MATERIALS_ON_HAND("query_analyze_storage_for_materials",  AnalyseMaterialsHandler.class, true),
    ANALYZE_STAR_SYSTEM_EXPLORATION("query_exploration_profits_data",  AnalyzeExplorationProfitsHandler.class, true),
    ANALYZE_CURRENT_PLANET("query_current_location",  AnalyzeCurrentLocationHandler.class, true),
    ANALYZE_BODY_MATERIALS("query_planetary_materials_present_on_planet",  AnalyzeMaterialsOnPlanetHandler.class, true),
    ANALYZE_EXO_BIOLOGY("query_organic_and_exobiology_data_for_current_planet",  AnalyzeBioSamplesHandler.class, true),
    ANALYZE_CURRENT_FUEL_STATUS("query_ship_fuel_status",  AnalyzeFuelStatusHandler.class, true),
    ANALYZE_FSD_TARGET("query_analyze_fsd_target",  AnalyzeFsdTargetHandler.class, true),
    ANALYZE_TRADE_ROUTE("query_trade_route_analysis",  AnalyzeTradeRouteHandler.class, true),
    ANALYZE_LOCAL_OUTFITTING("query_local_outfitting",  AnalyzeLocalOutfittingHandler.class, true),
    ANALYZE_LOCAL_SHIPYARD("query_local_shipyard",  AnalyzeShipyardHandler.class, true),
    ANALYZE_LOCAL_STATIONS("query_local_stations_services",  AnalyzeLocalStations.class, true),
    HOW_FAR_TO_FINAL_DESTINATION("query_ship_distance_to_final_destination",  AnalyzeDistanceToFinalDestination.class, false),
    QUERY_ANALYZE_ON_BOARD_CARGO("query_ship_cargo_contents_for_commodities",  AnalyzeCargoHoldHandler.class, true),
    QUERY_ANALYZE_ROUTE("query_analyze_ship_plotted_route",  AnalyzeRouterHandler.class, true),
    QUERY_ANALYZE_FUEL_ROUTE("query_can_we_fuel_on_route",  AnalyzeRouterHandler.class, true),
    ANALYZE_CARRIER_ROUTE("query_fleet_carrier_route",  AnalyzeCarrierRouteHandler.class, true),
    ANALYZE_FLEET_CARRIER_FUEL_SUPPLY("query_fleet_carrier_tritium_fuel_supply",  AnalyzeFleetCarrierFuelSupplyHandler.class, true),
    WHAT_IS_OUR_CARRIER_DESTINATION("query_what_is_our_fleet_carrier_destination",  AnalyzeFleetCarrierFinalDestinationHandler.class, true),
    QUERY_CARRIER_STATS("query_fleet_carrier_info_range_fuel_etc",  AnalyzeCarrierDataHandler.class, true),
    CARRIER_ETA("query_fleet_carrier_arrival_eta",  CarrierETAHandler.class, false),
    HOW_FAR_IS_OUR_CARRIER("query_distance_to_fleet_carrier",  AnalyzeDistanceFromFleetCarrierHandler.class, false),

    /// First query might be enough. TODO: Check.
    QUERY_MISSIONS("query_summarize_outstanding_missions",  AnalyzePirateMissionHandler.class, true),
    QUERY_PIRATE_MISSION_KILLS_REMAINING("query_pirate_mission_remaining_kills_profit_or_progress",  AnalyzePirateMissionHandler.class, true),
    //QUERY_PIRATE_MISSION_PROFIT("query_analyze_pirate_mission_profit",  AnalyzePirateMissionHandler.class, true),
    //QUERY_PIRATE_MISSION_STATUS("query_analyze_pirate_mission_progress",  AnalyzePirateMissionHandler.class, true),
    QUERY_NEXT_STAR_SCOOPABLE("query_next_star_fuel",  AnalyzeNextStarForFuelHandler.class, false),
    QUERY_PLAYER_STATS_ANALYSIS("query_analyze_player_profile",  AnalyzePlayerProfile.class, true),
    QUERY_SHIP_LOADOUT("query_ship_loadout_details",  AnalyzeShipLoadoutHandler.class, true),
    STATION_DATA("query_station_details",  StationDataHandler.class, true),
    
    WHAT_ARE_YOUR_CAPABILITIES("query_app_capabilities",  WhatAreYourCapabilitiesHandler.class, false),
    WHAT_IS_YOUR_DESIGNATION("query_ai_designation",  WhatIsYourNameHandler.class, false),
    TOTAL_BOUNTIES_COLLECTED("query_bounties_collected",  AnalyzeBountiesCollectedHandler.class, false),
    HOW_FAR_ARE_WE_FROM_BUBBLE("query_distance_to_bubble",  AnalyzeDistanceFromTheBubble.class, false),
    HOW_FAR_ARE_WE_FROM_LAST_BIO_SAMPLE("query_distance_to_last_exobiology_sample",  AnalyzeDistanceFromLastBioSample.class, true),
    TIME_IN_ZONE("what_time_is_it_in",  TimeQueryHandler.class, true),
    PERFORM_PRELIMINARY_BIOME_ANALYSIS("query_analyse_biome_for_planet_or_planets",  PlanetBiomeAnalyzerHandler.class, true),

    REMINDER("query_remind_me",  RemindTargetDestinationHandler.class, false),
    GENERAL_CONVERSATION("general_conversation",  ConversationalQueryHandler.class, false);


    private final String action;
    private final Class<? extends QueryHandler> handlerClass;
    private final boolean requiresFollowUp;

    Queries(String action, Class<? extends QueryHandler> handlerClass, boolean requiresFollowUp) {
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
package elite.intel.ai.brain.handlers.query;

import elite.intel.ai.brain.handlers.query.struct.AnalyseMaterialsHandler;

// @formatter:off
public enum Queries {

    /// ########################################################################################################################################
    HELP                                ("help",                                        HelpHandler.class),
    BIO_SAMPLE_IN_STAR_SYSTEM           ("query_organic_samples_star_system",           AnalyzeBioSignalsStarSystemHandler.class),
    EXOBIOLOGY_SAMPLES                  ("query_organic_samples_this_planet",           AnalyzeBioSamplesPlanetSurfaceHandler.class),
    QUERY_STELLAR_OBJETS                ("query_data_for_stellar_objects_planets_moons",AnalyzeStellarObjectsHandler.class),
    QUERY_STELLAR_SIGNALS               ("query_for_detected_signals",                  AnalyzeStellarSignalsHandler.class),
    QUERY_GEO_SIGNALS                   ("query_for_geo_signals",                       AnalyzeGeologyInStarSystemHandler.class),
    QUERY_STATIONS                      ("query_for_stations_and_ports",                AnalyzeStationsHandler.class),
    QUERY_CARRIERS                      ("query_for_fleet-carriers",                    AnalyzeCarriersHandler.class),
    KEY_BINDINGS_ANALYSIS               ("query_key_bindings_analysis",                 AnalyzeMisingKeyBindingHandler.class),
    SYSTEM_SECURITY_ANALYSIS            ("query_security_analysis",                     AnalyzeSystemSecurityHandler.class),
    TRADE_PROFILE_ANALYSIS              ("query_trade_profile_analysis",                AnalyzeTradeProfileHandler.class),
    DISTANCE_TO_BODY                    ("query_distance_to_stellar_object",            AnalyzeDistanceToStellarObject.class),
    LAST_SCAN_ANALYSIS                  ("query_last_scan_analysis",                    AnalyzeLastScanHandler.class),
    MATERIALS_INVENTORY                 ("query_inventory",                             AnalyseMaterialsHandler.class),
    PLANET_MATERIALS                    ("query_materials_present_on_planet",           AnalyzeMaterialsOnPlanetHandler.class),
    EXPLORATION_PROFITS                 ("query_expobiology_and_exploration_profits",   AnalyzeExplorationProfitsHandler.class),
    CURRENT_LOCATION                    ("query_current_location_analysis",             AnalyzeCurrentLocationHandler.class),
    SHIP_FUEL_STATUS                    ("query_ship_fuel_status",                      AnalyzeFuelStatusHandler.class),
    FSD_TARGET_ANALYSIS                 ("query_analyse_selected_target",               AnalyzeFsdTargetHandler.class),
    TRADE_ROUTE_ANALYSIS                ("query_trade_route_analysis",                  AnalyzeTradeRouteHandler.class),
    LOCAL_OUTFITTING                    ("query_local_outfitting",                      AnalyzeLocalOutfittingHandler.class),
    LOCAL_SHIPYARD                      ("query_local_shipyard",                        AnalyzeShipyardHandler.class),
    //LOCAL_STATIONS                      ("query_local_stations",                        AnalyzeLocalStations.class),
    DISTANCE_TO_DESTINATION             ("query_distance_to_destination",               AnalyzeDistanceToFinalDestination.class),
    CARGO_HOLD_CONTENTS                 ("query_cargo_hold_contents",                   AnalyzeCargoHoldHandler.class),
    PLOTTED_ROUTE_ANALYSIS              ("query_ship_route_analysis",                   AnalyzeRouterHandler.class),
    CARRIER_ROUTE_ANALYSIS              ("query_carrier_route_analysis",                AnalyzeCarrierRouteHandler.class),
    CARRIER_TRITIUM_SUPPLY              ("query_carrier_fuel_supply",                   AnalyzeFleetCarrierFuelSupplyHandler.class),
    CARRIER_DESTINATION                 ("query_carrier_destination",                   AnalyzeFleetCarrierFinalDestinationHandler.class),
    CARRIER_STATUS                      ("query_carrier_statistics",                    AnalyzeCarrierDataHandler.class),
    CARRIER_ETA                         ("query_carrier_eta",                           CarrierETAHandler.class),
    DISTANCE_TO_CARRIER                 ("query_distance_to_carrier",                   AnalyzeDistanceFromFleetCarrierHandler.class),
    // todo: dual usage of analyze pirate_missions, conflicting querying
//    OUTSTANDING_PIRATE_MISSIONS         ("query_outstanding_pirate_missions",           AnalyzePirateMissionHandler.class),
    PIRATE_MISSION_PROGRESS             ("query_pirate_mission_progress",               AnalyzePirateMissionHandler.class),
    PLAYER_PROFILE_ANALYSIS             ("query_player_profile_analysis",               AnalyzePlayerProfile.class),
    SHIP_LOADOUT                        ("query_ship_loadout",                          AnalyzeShipLoadoutHandler.class),
    STATION_DETAILS                     ("query_station_details",                       StationDataHandler.class),
    APP_CAPABILITIES                    ("query_app_capabilities",                      WhatAreYourCapabilitiesHandler.class),
    AI_DESIGNATION                      ("query_ai_designation",                        WhatIsYourNameHandler.class),
    TOTAL_BOUNTIES                      ("query_total_bounties",                        AnalyzeBountiesCollectedHandler.class),
    DISTANCE_TO_BUBBLE                  ("query_distance_to_bubble",                    AnalyzeDistanceFromTheBubble.class),
    DISTANCE_TO_LAST_BIO_SAMPLE         ("query_distance_to_last_bio_sample",           AnalyzeDistanceFromLastBioSample.class),
    TIME_IN_ZONE                        ("query_what_time_is_it_on_earth",              TimeQueryHandler.class),
    PLANET_BIOME_ANALYSIS               ("query_biome_analysis",                        BiomeAnalyzerHandler.class),
    REMINDER                            ("query_reminder",                              RemindTargetDestinationHandler.class),
    GENERAL_CONVERSATION                ("general_conversation",                        ConversationalQueryHandler.class),
    ANALYZE_MISSIONS                    ("query_analyze_missions",                      AnalyzeMissionHandler.class);
    /// ########################################################################################################################################


    private final String action;
    private final Class<? extends QueryHandler> handlerClass;

    Queries(String action, Class<? extends QueryHandler> handlerClass) {
        this.action = action;
        this.handlerClass = handlerClass;
    }

    public String getAction() {
        return action;
    }

    public Class<? extends QueryHandler> getHandlerClass() {
        return handlerClass;
    }
}
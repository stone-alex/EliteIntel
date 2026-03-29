package elite.intel.ai.brain.handlers.query;

// @formatter:off
public enum Queries {

    /// ########################################################################################################################################
    ///HELP                                ("help",                                         HelpHandler.class),
    BIO_SAMPLE_IN_STAR_SYSTEM           ("query_bio_scan_progress",                     AnalyzeBioSignalsStarSystemHandler.class),
    EXOBIOLOGY_SAMPLES                  ("query_exobiology_samples",                    AnalyzeBioSamplesPlanetSurfaceHandler.class),
    QUERY_STELLAR_OBJETS                ("query_stellar_objects",                       AnalyzeStellarObjectsHandler.class),
    QUERY_STELLAR_SIGNALS               ("query_signals",                               AnalyzeStellarSignalsHandler.class),
    QUERY_GEO_SIGNALS                   ("query_geo_signals",                           AnalyzeGeologyInStarSystemHandler.class),
    QUERY_STATIONS                      ("query_stations",                              AnalyzeStationsHandler.class),
    QUERY_CARRIERS                      ("query_carriers",                              AnalyzeCarriersHandler.class),
    KEY_BINDINGS_ANALYSIS               ("query_key_bindings",                          AnalyzeMisingKeyBindingHandler.class),
    SYSTEM_SECURITY_ANALYSIS            ("query_system_security",                       AnalyzeSystemSecurityHandler.class),
    TRADE_PROFILE_ANALYSIS              ("query_trade_profile",                         AnalyzeTradeProfileHandler.class),
    DISTANCE_TO_BODY                    ("query_distance_to_body",                      AnalyzeDistanceToStellarObject.class),
    LAST_SCAN_ANALYSIS                  ("query_last_scan",                             AnalyzeLastScanHandler.class),
    MATERIALS_INVENTORY                 ("query_material_inventory",                    AnalyseMaterialsHandler.class),
    PLANET_MATERIALS                    ("query_planet_materials",                      AnalyzeMaterialsOnPlanetHandler.class),
    EXPLORATION_PROFITS                 ("query_exploration_profits",                   AnalyzeExplorationProfitsHandler.class),
    CURRENT_LOCATION                    ("query_current_location",                      AnalyzeCurrentLocationHandler.class),
    SHIP_FUEL_STATUS                    ("query_fuel_status",                           AnalyzeFuelStatusHandler.class),
    FSD_TARGET_ANALYSIS                 ("query_fsd_target",                            AnalyzeFsdTargetHandler.class),
    TRADE_ROUTE_ANALYSIS                ("query_trade_route",                           AnalyzeTradeScheduleHandler.class),
    LOCAL_OUTFITTING                    ("query_local_outfitting",                      AnalyzeLocalOutfittingHandler.class),
    LOCAL_SHIPYARD                      ("query_local_shipyard",                        AnalyzeShipyardHandler.class),
    CARGO_HOLD_CONTENTS                 ("query_cargo_hold_contents",                            AnalyzeCargoHoldHandler.class),
    PLOTTED_ROUTE_ANALYSIS              ("query_plotted_route",                         AnalyzeRouterHandler.class),
    CARRIER_ROUTE_ANALYSIS              ("query_carrier_route",                         AnalyzeCarrierRouteHandler.class),
    CARRIER_TRITIUM_SUPPLY              ("query_carrier_fuel",                          AnalyzeFleetCarrierFuelSupplyHandler.class),
    CARRIER_DESTINATION                 ("query_carrier_destination",                   AnalyzeFleetCarrierFinalDestinationHandler.class),
    CARRIER_STATUS                      ("query_carrier_status",                        AnalyzeCarrierDataHandler.class),
    CARRIER_ETA                         ("query_carrier_eta",                           CarrierETAHandler.class),
    DISTANCE_TO_CARRIER                 ("query_distance_to_carrier",                   AnalyzeDistanceFromFleetCarrierHandler.class),
    PIRATE_MISSION_PROGRESS             ("query_pirate_mission",                        AnalyzePirateMissionHandler.class),
    PLAYER_PROFILE_ANALYSIS             ("query_player_profile",                        AnalyzePlayerProfile.class),
    SHIP_LOADOUT                        ("query_ship_loadout",                          AnalyzeShipLoadoutHandler.class),
    STATION_DETAILS                     ("query_station_details",                       StationDataHandler.class),
    APP_CAPABILITIES                    ("query_app_capabilities",                      WhatAreYourCapabilitiesHandler.class),
    AI_DESIGNATION                      ("query_ai_designation",                        WhatIsYourNameHandler.class),
    TOTAL_BOUNTIES                      ("query_total_bounties",                        AnalyzeBountiesCollectedHandler.class),
    DISTANCE_TO_BUBBLE                  ("query_distance_to_bubble",                    AnalyzeDistanceFromTheBubble.class),
    DISTANCE_TO_LAST_BIO_SAMPLE         ("query_distance_to_bio_sample",                AnalyzeDistanceFromLastBioSample.class),
    TIME_IN_ZONE                        ("query_time",                                  TimeQueryHandler.class),
    PLANET_BIOME_ANALYSIS               ("query_biome_analysis",                        BiomeAnalyzerHandler.class),
    REMINDER                            ("query_reminder",                              RemindTargetDestinationHandler.class),
    ANALYZE_MISSIONS                    ("query_missions_and_rewards",                              AnalyzeMissionHandler.class),
    ANALYZE_LOCAL_STATIONS              ("query_local_stations",                        AnalyzeLocalStations.class),
    GENERAL_CONVERSATION                ("query_general_conversation",                  GeneralConversationHandler.class)
    ;
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
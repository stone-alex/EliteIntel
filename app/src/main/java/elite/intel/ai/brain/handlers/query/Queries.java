package elite.intel.ai.brain.handlers.query;

import elite.intel.ai.brain.handlers.query.struct.AnalyseMaterialsHandler;

// @formatter:off
public enum Queries {


    /// ########################################################################################################################################
    HELP                                ("help",                                        HelpHandler.class, true),
    BIO_SAMPLE_IN_STAR_SYSTEM           ("query_star_system_for_organic_samples",       AnalyzeBioSignalsHandler.class, true),
    FSS_SIGNALS                         ("query_star_system_for_fss_signals",           AnalyzeFssSignalsHandler.class, true),
    QUERY_STELLAR_OBJETS                ("query_star_system_for_stellar_objects",       AnalyzeStrellarObjectsHandler.class, true),
    QUERY_GEO_SIGNALS                   ("query_star_system_for_geo_signals",           AnalyzeGeologyInStarSystemHandler.class, true),
    QUERY_STATIONS                      ("query_star_system_for__stations",             AnalyzeStationsHandler.class, true),
    KEY_BINDINGS_ANALYSIS               ("query_key_bindings_analysis",                 AnalyzeMisingKeyBindingHandler.class, true),
    SYSTEM_SECURITY_ANALYSIS            ("query_star_system_security_analysis",         AnalyzeSystemSecurityHandler.class, true),
    TRADE_PROFILE_ANALYSIS              ("query_trade_profile_analysis",                AnalyzeTradeProfileHandler.class, true),
    DISTANCE_TO_BODY                    ("query_distance_to_body",                      AnalyzeDistanceToStellarObject.class, true),
    LAST_SCAN_ANALYSIS                  ("query_last_scan_analysis",                    AnalyzeLastScanHandler.class, true),
    MATERIALS_INVENTORY                 ("query_inventory",                             AnalyseMaterialsHandler.class, true),
    PLANET_MATERIALS                    ("query_materials_present_on_planet",           AnalyzeMaterialsOnPlanetHandler.class, true),
    EXPLORATION_PROFITS                 ("query_exploration_profits",                   AnalyzeExplorationProfitsHandler.class, true),
    CURRENT_LOCATION                    ("query_current_location",                      AnalyzeCurrentLocationHandler.class, true),
    EXOBIOLOGY_SAMPLES                  ("query_exobiology_samples",                    AnalyzeBioSamplesHandler.class, true),
    SHIP_FUEL_STATUS                    ("query_ship_fuel_status",                      AnalyzeFuelStatusHandler.class, true),
    FSD_TARGET_ANALYSIS                 ("query_analyse_selected_target",               AnalyzeFsdTargetHandler.class, true),
    TRADE_ROUTE_ANALYSIS                ("query_trade_route_analysis",                  AnalyzeTradeRouteHandler.class, true),
    LOCAL_OUTFITTING                    ("query_local_outfitting",                      AnalyzeLocalOutfittingHandler.class, true),
    LOCAL_SHIPYARD                      ("query_local_shipyard",                        AnalyzeShipyardHandler.class, true),
    LOCAL_STATIONS                      ("query_local_stations",                        AnalyzeLocalStations.class, true),
    DISTANCE_TO_DESTINATION             ("query_distance_to_destination",               AnalyzeDistanceToFinalDestination.class, false),
    CARGO_HOLD_CONTENTS                 ("query_cargo_hold_contents",                   AnalyzeCargoHoldHandler.class, true),
    PLOTTED_ROUTE_ANALYSIS              ("query_plotted_route_analysis",                AnalyzeRouterHandler.class, true),
    ROUTE_FUEL_CHECK                    ("query_route_fuel_check",                      AnalyzeRouterHandler.class, true),
    CARRIER_ROUTE_ANALYSIS              ("query_carrier_route_analysis",                AnalyzeCarrierRouteHandler.class, true),
    CARRIER_TRITIUM_SUPPLY              ("query_carrier_fuel_supply",                   AnalyzeFleetCarrierFuelSupplyHandler.class, true),
    CARRIER_DESTINATION                 ("query_carrier_destination",                   AnalyzeFleetCarrierFinalDestinationHandler.class, true),
    CARRIER_STATUS                      ("query_carrier_status",                        AnalyzeCarrierDataHandler.class, true),
    CARRIER_ETA                         ("query_carrier_eta",                           CarrierETAHandler.class, false),
    DISTANCE_TO_CARRIER                 ("query_distance_to_carrier",                   AnalyzeDistanceFromFleetCarrierHandler.class, false),
    OUTSTANDING_MISSIONS                ("query_outstanding_missions",                  AnalyzePirateMissionHandler.class, true),
    PIRATE_MISSION_PROGRESS             ("query_pirate_mission_progress",               AnalyzePirateMissionHandler.class, true),
    NEXT_SCOOPABLE_STAR                 ("query_next_scoopable_star",                   AnalyzeNextStarForFuelHandler.class, false),
    PLAYER_PROFILE_ANALYSIS             ("query_player_profile_analysis",               AnalyzePlayerProfile.class, true),
    SHIP_LOADOUT                        ("query_ship_loadout",                          AnalyzeShipLoadoutHandler.class, true),
    STATION_DETAILS                     ("query_station_details",                       StationDataHandler.class, true),
    APP_CAPABILITIES                    ("query_app_capabilities",                      WhatAreYourCapabilitiesHandler.class, false),
    AI_DESIGNATION                      ("query_ai_designation",                        WhatIsYourNameHandler.class, false),
    TOTAL_BOUNTIES                      ("query_total_bounties",                        AnalyzeBountiesCollectedHandler.class, false),
    DISTANCE_TO_BUBBLE                  ("query_distance_to_bubble",                    AnalyzeDistanceFromTheBubble.class, false),
    DISTANCE_TO_LAST_BIO_SAMPLE         ("query_distance_to_last_bio_sample",           AnalyzeDistanceFromLastBioSample.class, true),
    TIME_IN_ZONE                        ("query_time_in_zone",                          TimeQueryHandler.class, true),
    PLANET_BIOME_ANALYSIS               ("query_planet_biome_analysis",                 PlanetBiomeAnalyzerHandler.class, true),
    REMINDER                            ("query_reminder",                              RemindTargetDestinationHandler.class, false),
    GENERAL_CONVERSATION                ("general_conversation",                        ConversationalQueryHandler.class, false);
    /// ########################################################################################################################################


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
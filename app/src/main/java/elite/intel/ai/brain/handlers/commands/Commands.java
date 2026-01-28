package elite.intel.ai.brain.handlers.commands;

import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;

/**
 * CommandActionsCustom represents an enumeration of customizable command actions,
 * each associated with specific functionality in the system. This enum facilitates
 * the mapping of commands to their corresponding handlers, placeholders, and parameter keys.
 * <p>
 * Each command action consists of:
 * - A string representing the action keyword.
 * - An optional placeholder for parameters within the command.
 * - A parameter key used to store or retrieve the parameter value from a session.
 * - A handler class responsible for processing the command's execution logic.
 * <p>
 * CommandActionsCustom is used to centralize and define the behavior for a variety
 * of commands, ensuring a consistent mechanism for handling user input and executing
 * corresponding actions.
 */

// @formatter:off
public enum Commands {

    NAVIGATE_TO_NEXT_MISSION                            ("plot_route_to_next_mission",null,null, PlotRouteToNextMissionDestination.class),
    LIGHTS_ON_OFF                                       ("toggle_lights_on_off", null, "state", LightsOnOffHandler.class),
    ADD_MINING_TARGET                                   ("add_mining_target", null, "key", AddMiningTargetHandler.class),
    CLEAR_MINING_TARGETS                                ("clear_mining_targets", null, null, ClearMiningTargetsHandler.class),
    CLEAR_CACHE                                         ("clear_cache", null, "session_clear", ClearCacheHandler.class),
    INTERRUPT_TTS                                       ("shut_up_cancel_interrupt_tts_vocalization", null, null, ShutUpHandler.class),

    FIND_RAW_MATERIAL_TRADER                            ("find_raw_material_trader", null, "key", FindRawMaterialTraderHandler.class),
    FIND_ENCODED_MATERIAL_TRADER                        ("find_encoded_material_trader", null, "key", FindEncodedMaterialTraderHandler.class),
    FIND_MANUFACTURED_MATERIAL_TRADER                   ("find_manufactured_material_trader", null, "key", FindManufacturedMaterialTraderHandler.class),

    FIND_HUNTING_GROUNDS                                ("find_hunting_grounds_for_pirate_massacre_missions", null, "key", LocatePirateHuntingGrounds.class),
    RECON_TARGET_SYSTEM                                 ("navigate_plot_reconnaissance_route_to_hunting_grounds_target_star_system", null, null, ReconPirateMissionTargetSystemHandler.class),
    RECON_PROVIDER_SYSTEM                               ("navigate_plot_reconnaissance_route_to_mission_provider_system", null, null, ReconMissionProviderSystemHandler.class),
    NAVIGATE_TO_PIRATE_MISSION_TARGET_SYSTEM            ("plot_route_to_pirate_massacre_mission_target_system", null, null, NavigateToPirateMassacreMissionTargetHandler.class),

    NAVIGATE_TO_MISSION_BATTLE_GROUND                   ("navigate_to_mission_battle_ground", null, null, PloteRouteToPirateMissionArena.class),

    MONETIZE_ROUTE                                      ("monetize_route", null, null, MonetizeRouteHandler.class),

    FIND_HUMAN_TECHNOLOGY_BROKER                        ("find_human_technology_broker", null, "key", FindHumanTechnologyBrokerHandler.class),
    FIND_GUARDIAN_TECHNOLOGY_BROKER                     ("find_guardian_technology_broker", null, "key", FindGuadrianTechnologyBroker.class),

    FIND_VISTA_GENOMICS                                 ("find_vista_genomics", null, "key", FindVistaGenomicsHandler.class),
    FIND_BRAIN_TREES                                    ("find_brain_trees", null, "key", FindBrainTreesHandler.class),
    FIND_FLEET_CARRIER_FUEL_MINING_SITE                 ("find_fleet_carrier_fuel_mining_site", null, "key", FindCarrierFuelMiningSiteHandler.class),
    FIND_MINING_SITE                                    ("find_mining_site_for_material", null, "material , max_distance", FindMiningSiteHandler.class),

    FIND_NEAREST_FLEET_CARRIER                          ("find_nearest_fleet_carrier", null, "key", FindNearestFleetCarrierHandler.class),
    CLEAR_FLEET_CARRIER_ROUTE                           ("clear_fleet_carrier_route", null, null, ClearFleetCarrierRouteHandler.class),

    FIND_COMMODITY                                      ("find_market_where_to_buy", null, "key , max_distance", FindCommodityHandler.class),
    SET_AI_VOICE                                        ("set_or_change_voice_to", null, "key", ChangeAiVoiceHandler.class),
    SET_HOME_SYSTEM                                     ("set_location_as_home_star_system", null, null, SetCurrentStarAsHomeSystem.class),
    SET_PERSONALITY                                     ("set_personality", null, "personality", SetPersonalityHandler.class),
    SET_PROFILE                                         ("set_profile", null, "profile", SetCadenceHandler.class),
    SET_RADIO_TRANSMISSION_MODE                         ("turn_radio_transmission_on_off", null, "state", SetRadioTransmissionOnOff.class),
    SET_STREAMING_MODE                                  ("toggle_streaming_mode", null, "state", SetStreamingModeHandler.class),
    NAVIGATE_TO_TARGET                                  ("navigate_to_coordinates", null, "lat && lon", NavigateToCoordinatesHandler.class),
    NAVIGATION_ON_OFF                                   ("cancel_navigation", null, null, NavigationOnOffHandler.class),
    DISCOVERY_ON_OFF                                    ("toggle_discovery_announcements", null, "state", DiscoveryOnOffHandler.class),
    MINING_ON_OFF                                       ("toggle_mining_announcements", null, "state", MiningOnOffHandler.class),
    ROUTE_ON_OFF                                        ("toggle_route_announcements", null, "state", RouteAnnouncementsOnOffHandler.class),

    INCREASE_SPEED_BY                                   ("speed_plus", BINDING_INCREASE_SPEED.getGameBinding(), "key", SpeedControlHandler.class),
    DECREASE_SPEED_BY                                   ("speed_minus", BINDING_DECREASE_SPEED.getGameBinding(), "key", SpeedControlHandler.class),

    /// Commands that have a specific handler impl (which uses N bindings inside)
    INCREASE_ENGINES_POWER                              ("transfer_power_to_engines", null, null, SetPowerToEnginesHandler.class),
    NAVIGATE_TO_NEXT_BIO_SAMPLE                         ("navigate_to_next_organic_codex_entry", null, null, NavigateToNextCodexEntry.class),
    INCREASE_SHIELDS_POWER                              ("transfer_power_to_shields", null, null, SetPowerToSystemsHandler.class),
    INCREASE_SYSTEMS_POWER                              ("transfer_power_to_ship_systems", null, null, SetPowerToSystemsHandler.class),
    INCREASE_WEAPONS_POWER                              ("transfer_power_to_weapons", null, null, SetPowerToWeaponsHandler.class),
    RESET_POWER                                         ("equalize_power_settings", null, null, ResetPowerSettings.class),

    OPEN_GALAXY_MAP                                     ("open_galaxy_star_map", null, null, OpenGalaxyMapHandler.class),
    OPEN_SYSTEM_MAP                                     ("open_local_system_map", null, null, OpenSystemMapHandler.class),
    CLOSE_ANY_MAP                                       ("close_map", null, null, ExitToHud.class),
    EXIT_TO_HUD                                         ("display_hud", null, null, ExitToHud.class),
    EXIT                                                ("exit", null, null, ExitToHud.class),

    DISPLAY_COMMS_PANEL                                 ("display_comms_panel", null, null, DisplayCommsPanelHandler.class),
    DISPLAY_CONTACTS_PANEL                              ("display_contacts", null, null, DisplayContactsPanelHandler.class),
    DISPLAY_INTERNAL_PANEL                              ("display_internal_panel", null, null, DisplayInternalPanelHandler.class),
    DISPLAY_STATUS_PANEL                                ("display_status_panel", null, null, DisplayStatusPanelHandler.class),
    DISPLAY_RADAR_PANEL                                 ("display_radar_panel", null, null, DisplayRadarPanelHandler.class),
    DISPLAY_LOADOUT_PANEL                               ("display_loadout_panel", null, null, DisplayLoadoutPanelHandler.class),
    DISPLAY_CARRIER_MANAGEMENT                          ("display_carrier_management", null, null, OpenFleetCarrierManagementHandler.class    ),
    OPEN_CARGO_SCOOP                                    ("open_cargo_scoop", null, null, OpenCargoScoopHandler.class),
    CLOSE_CARGO_SCOOP                                   ("close_cargo_scoop", null, null, CloseCargoScoopHandler.class),
    RETRACT_HARDPOINTS                                  ("retract_hardpoints", null, null, RetractHardpointsHandler.class),
    DEPLOY_HARDPOINTS                                   ("deploy_hardpoints", null, null, DeployHardpointsHandler.class),
    DEPLOY_LANDING_GEAR                                 ("deploy_landing_gear_down", null, null, DeployLandingGearHandler.class),
    RETRACT_LANDING_GEAR                                ("retract_landing_gear_up", null, null, RetractLandingGearHandler.class),

    JUMP_TO_HYPERSPACE                                  ("jump_to_hyperspace", null, null, JumpToHyperspaceHandler.class),
    ENTER_SUPER_CRUISE                                  ("enter_super_cruise", null, null, EnterFtlHandler.class),

    EXIT_SUPER_CRUISE                                   ("exit_super_cruise", null, null, ExitFtlHandler.class),
    ACTIVATE_ANALYSIS_MODE                              ("swap_to_hud_analysis_mode", null, null, ActivateAnalysisModeHandler.class),
    ACTIVATE_COMBAT_MODE                                ("swap_to_hud_combat_mode", null, null, ActivateCombatModeHandler.class),
    PLOT_ROUTE_TO_CARRIER                               ("plot_route_to_fleet_carrier", null, null, PlotRouteToMyFleetCarrier.class),
    SET_OPTIMAL_SPEED                                   ("set_optimal_speed", null, null, SetOptimalSpeedHandler.class),
    TAKE_ME_HOME                                        ("plot_route_to_home_star", null, null, PlotRouteToHomeHandler.class),
    OPEN_FSS_AND_SCAN                                   ("open_fss_to_scan_or_honk_star_system", null, null, DisplayFssAndScanHandler.class),

    GET_HEADING_TO_LZ                                   ("navigate_bearing_direction_to_landing_zone", null, null, NavigateToLandingZone.class),
    DEPLOY_SRV                                          ("deploy_srv", null, null, DeploySrvHandler.class),
    RECOVER_SRV                                         ("recover_srv", null, null, BoardSrvHandler.class),
    CLEAR_CODEX_ENTRIES                                 ("clear_codex_entries", null, null, ClearCodexEntriesHandler.class),
    CALCULATE_FLEET_CARRIER_ROUTE                       ("calculate_fleet_carrier_route", null, null, CalculateFleetCarrierRouteHandler.class),
    ENTER_NEXT_FLEET_CARRIER_DESTINATION                ("enter_next_fleet_carrier_destination", null, null, EnterNextCarrierDestinationHandler.class),
    SHUT_DOWN                                           ("system_shut_down", null, null, SystemShutDownRequestHandler.class),

    CALCULATE_TRADE_ROUTE                               ("calculate_trade_route", null, null, CalculateTradeRouteHandler.class),
    PLOT_ROUTE_TO_NEXT_TRADE_STOP                       ("navigate_to_next_trade_stop_port_or_station", null, null, PlotRouteToNextTradeStopHandler.class),
    CHANGE_TRADE_PROFILE_SET_STARTING_BUDGET            ("alter_trade_profile_set_starting_budget", null, "key", ChangeTradeProfileSetStartingBudgetHander.class),
    CHANGE_TRADE_PROFILE_SET_MAX_NUMBER_OF_STOPS        ("alter_trade_profile_set_maximum_number_of_stops", null, "key", ChangeTradeProfileSetMaxStopsHandler.class),
    CHANGE_TRADE_PROFILE_SET_MAX_DISTANCE_FROM_ENTRY    ("alter_trade_profile_set_maximum_distance_from_entry", null, "key", ChangeTradeProfileSetMaxDistanceFromEntryHandler.class),
    CHANGE_TRADE_PROFILE_SET_ALLOW_PROHIBITED_CARGO     ("alter_trade_profile_toggle_prohibited_cargo", null, "state", ChangeTradeProfileSetAllowProhibitedCargoHandler.class),
    CHANGE_TRADE_PROFILE_SET_ALLOW_PLANETARY_PORT       ("alter_trade_profile_toggle_planetary_ports", null, "state", ChangeTradeProfileSetIncluidePlanetaryPortsHandler.class),
    CHANGE_TRADE_PROFILE_SET_ALLOW_PERMIT_SYSTEMS       ("alter_trade_profile_toggle_permit_protected_star_systems", null, "state", ChangeTradeProfileAllowPermitSystemsHandler.class),
    CHANGE_TRADE_PROFILE_SET_ALLOW_STRONGHOLDS          ("alter_trade_profile_toggle_strongholds", null, "state", ChangeTradeProfileSetAllowEnemyStrongHoldsHandler.class),
    LIST_TRADE_ROUTE_PARAMETERS                         ("list_available_trade_route_parameters", null, null, ListAvailableTradeRouteProfilesHandler.class),
    CLEAR_TRADE_ROUTE                                   ("clear_trade_route", null, null, ClearTradeRouteHandler.class),

    /// Generic simple commands. no parameters, but require binding
    ACTIVATE                                            ("activate", BINDING_ACTIVATE.getGameBinding(), null, GenericGameControlHandler.class),

    REQUEST_DOCKING                                     ("request_docking", null, null, RequestDockingHandler.class),

    NIGHT_VISION_ON_OFF                                     ("toggle_night_vision_on_off", BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), "state", ToggleNightVision.class),

    //CYCLE_NEXT_PAGE                                     ("cycle_next_page", BINDING_CYCLE_NEXT_PAGE.getGameBinding(), null, GenericGameControlHandler.class),
    //CYCLE_NEXT_PANEL                                    ("cycle_next_panel", BINDING_CYCLE_NEXT_PANEL.getGameBinding(), null, GenericGameControlHandler.class),
    //CYCLE_PREVIOUS_PAGE                                 ("cycle_previous_page", BINDING_CYCLE_PREVIOUS_PAGE.getGameBinding(), null, GenericGameControlHandler.class),
    //CYCLE_PREVIOUS_PANEL                                ("cycle_previous_panel", BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding(), null, GenericGameControlHandler.class),
    //CYCLE_NEXT_SUBSYSTEM                                ("cycle_next_subsystem", BINDING_CYCLE_NEXT_SUBSYSTEM.getGameBinding(), null, GenericGameControlHandler.class),
    //CYCLE_PREVIOUS_SUBSYSTEM                            ("cycle_previous_subsystem", BINDING_CYCLE_PREVIOUS_SUBSYSTEM.getGameBinding(), null, GenericGameControlHandler.class),
    DEPLOY_HEAT_SINK                                    ("deploy_heat_sink", BINDING_DEPLOY_HEAT_SINK.getGameBinding(), null, GenericGameControlHandler.class),
    DRIVE_ASSIST                                        ("drive_assist", BINDING_DRIVE_ASSIST.getGameBinding(), null, GenericGameControlHandler.class),

    TARGET_SUB_SYSTEM                                   ("target_subsystem", null, "subsystem", TargetSubSystemHandler.class),
    RECALL_DISMISS_SHIP                                 ("recall_or_dismiss_ship", BINDING_RECALL_DISMISS_SHIP.getGameBinding(), null, DismissShip.class),

    REQUEST_DEFENSIVE_BEHAVIOUR                         ("fighter_orders_defend_fighter_order", BINDING_REQUEST_DEFENSIVE_BEHAVIOUR.getGameBinding(), null, GenericGameControlHandler.class),
    REQUEST_FOCUS_TARGET                                ("fighter_orders_attack_my_target_fighter_order", BINDING_REQUEST_DEFENSIVE_BEHAVIOUR.getGameBinding(), null, GenericGameControlHandler.class),
    REQUEST_HOLD_FIRE                                   ("fighter_orders_hold_your_fire_fighter_order", BINDING_REQUEST_HOLD_FIRE.getGameBinding(), null, GenericGameControlHandler.class),
    REQUEST_REQUEST_DOCK                                ("fighter_orders_recall", BINDING_REQUEST_REQUEST_DOCK.getGameBinding(), null, GenericGameControlHandler.class),
    STOP                                                ("stop", BINDING_SET_SPEED_ZERO.getGameBinding(), null, GenericGameControlHandler.class),
    SET_SPEED_ZERO                                      ("set_speed_to_zero_0", BINDING_SET_SPEED_ZERO.getGameBinding(), null, GenericGameControlHandler.class),
    SET_SPEED25                                         ("set_speed_to_slow_throttle_25", BINDING_SET_SPEED25.getGameBinding(), null, GenericGameControlHandler.class),
    SET_SPEED50                                         ("set_speed_to_medium_throttle_50", BINDING_SET_SPEED50.getGameBinding(), null, GenericGameControlHandler.class),
    SET_SPEED75                                         ("set_speed_to_optimal_throttle_75", BINDING_SET_SPEED75.getGameBinding(), null, GenericGameControlHandler.class),
    SET_SPEED100                                        ("set_speed_to_maximum_throttle_100", BINDING_SET_SPEED100.getGameBinding(), null, GenericGameControlHandler.class),

    SET_CARRIER_FUEL_RESERVE                            ("set_tritium_carrier_fuel_reserve", null, "key", SetFleetCarrierFuelReserveHandler.class),

    SELECT_HIGHEST_THREAT                               ("target_highest_threat", BINDING_SELECT_HIGHEST_THREAT.getGameBinding(), null, GenericGameControlHandler.class),
    TARGET_NEXT_ROUTE_SYSTEM                            ("select_next_system_in_route", BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), null, GenericGameControlHandler.class),

    TARGET_WINGMAN0                                     ("target_wingman_1", BINDING_TARGET_WINGMAN0.getGameBinding(), null, GenericGameControlHandler.class),
    TARGET_WINGMAN1                                     ("target_wingman_2", BINDING_TARGET_WINGMAN1.getGameBinding(), null, GenericGameControlHandler.class),
    TARGET_WINGMAN2                                     ("target_wingman_3", BINDING_TARGET_WINGMAN2.getGameBinding(), null, GenericGameControlHandler.class),
    WING_NAV_LOCK                                       ("lock_on_wingman", BINDING_WING_NAV_LOCK.getGameBinding(), null, GenericGameControlHandler.class),
    LIST_AVAILABLE_VOICES                               ("list_available_voices", null, null, ListAvailableVoices.class),
    ;

    ///
    private final String action;
    private final String binding;
    private final String parameters;
    private final Class<? extends CommandHandler> handlerClass;

    Commands(String action, String binding, String paramKey, Class<? extends CommandHandler> handlerClass) {
        this.action = action;
        this.binding = binding;
        this.parameters = paramKey;
        this.handlerClass = handlerClass;
    }

    public static String[] getCommands() {
        String[] result = new String[Commands.values().length];
        for (int i = 0; i < Commands.values().length; i++) {
            result[i] = Commands.values()[i].getAction();
        }
        return result;
    }

    public static String getGameBinding(String action) {
        Commands[] values = values();
        for (Commands command : values) {
            if (action.equalsIgnoreCase(command.getAction())) {
                return command.getBinding();
            }
        }
        return null;
    }

    public String getAction() {
        return action;
    }

    public String getParameters() {
        return parameters;
    }

    public String getBinding() {
        return binding;
    }

    public Class<? extends CommandHandler> getHandlerClass() {
        return handlerClass;
    }
}
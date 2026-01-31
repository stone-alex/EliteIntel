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

    NAVIGATE_TO_NEXT_MISSION                            ("plot_route_to_mission_location",null, PlotRouteToMissionDestination.class),
    LIGHTS_ON_OFF                                       ("toggle_lights_on_off", null, LightsOnOffHandler.class),
    ADD_MINING_TARGET                                   ("add_mining_target", null, AddMiningTargetHandler.class),
    CLEAR_MINING_TARGETS                                ("clear_mining_targets", null, ClearMiningTargetsHandler.class),
    CLEAR_CACHE                                         ("clear_cache", null, ClearCacheHandler.class),
    INTERRUPT_TTS                                       ("shut_up_cancel_interrupt_tts_vocalization", null,ShutUpHandler.class),

    FIND_RAW_MATERIAL_TRADER                            ("find_raw_material_trader", null, FindRawMaterialTraderHandler.class),
    FIND_ENCODED_MATERIAL_TRADER                        ("find_encoded_material_trader", null, FindEncodedMaterialTraderHandler.class),
    FIND_MANUFACTURED_MATERIAL_TRADER                   ("find_manufactured_material_trader", null, FindManufacturedMaterialTraderHandler.class),

    FIND_HUNTING_GROUNDS                                ("find_hunting_grounds_for_pirate_massacre_missions", null,  LocatePirateHuntingGrounds.class),
    RECON_TARGET_SYSTEM                                 ("navigate_plot_reconnaissance_route_to_hunting_grounds_target_star_system", null,  ReconPirateMissionTargetSystemHandler.class),
    RECON_PROVIDER_SYSTEM                               ("navigate_plot_reconnaissance_route_to_mission_provider_system", null,  ReconMissionProviderSystemHandler.class),
    NAVIGATE_TO_PIRATE_MISSION_TARGET_SYSTEM            ("plot_route_to_pirate_massacre_mission_target_system", null,  NavigateToPirateMassacreMissionTargetHandler.class),

    NAVIGATE_TO_MISSION_BATTLE_GROUND                   ("navigate_to_mission_battle_ground", null,  PloteRouteToPirateMissionArena.class),

    MONETIZE_ROUTE                                      ("monetize_route", null,  MonetizeRouteHandler.class),

    FIND_HUMAN_TECHNOLOGY_BROKER                        ("find_human_technology_broker", null,  FindHumanTechnologyBrokerHandler.class),
    FIND_GUARDIAN_TECHNOLOGY_BROKER                     ("find_guardian_technology_broker", null,  FindGuadrianTechnologyBroker.class),

    FIND_VISTA_GENOMICS                                 ("find_vista_genomics", null,  FindVistaGenomicsHandler.class),
    FIND_BRAIN_TREES                                    ("find_brain_trees", null,  FindBrainTreesHandler.class),
    FIND_FLEET_CARRIER_FUEL_MINING_SITE                 ("find_fleet_carrier_fuel_mining_site", null,  FindCarrierFuelMiningSiteHandler.class),
    FIND_MINING_SITE                                    ("find_mining_site_for_material", null, FindMiningSiteHandler.class),

    FIND_NEAREST_FLEET_CARRIER                          ("find_nearest_fleet_carrier", null,  FindNearestFleetCarrierHandler.class),
    CLEAR_FLEET_CARRIER_ROUTE                           ("clear_fleet_carrier_route", null,  ClearFleetCarrierRouteHandler.class),

    FIND_COMMODITY                                      ("find_market_where_to_buy", null, FindCommodityHandler.class),
    SET_AI_VOICE                                        ("set_or_change_voice_to", null,  ChangeAiVoiceHandler.class),
    SET_HOME_SYSTEM                                     ("set_location_as_home_star_system", null,  SetCurrentStarAsHomeSystem.class),
    SET_PERSONALITY                                     ("set_personality", null, SetPersonalityHandler.class),
    SET_PROFILE                                         ("set_profile", null, SetCadenceHandler.class),
    SET_RADIO_TRANSMISSION_MODE                         ("turn_radio_transmission_on_off", null,  SetRadioTransmissionOnOff.class),
    SET_STREAMING_MODE                                  ("toggle_streaming_mode", null, SetStreamingModeHandler.class),
    NAVIGATE_TO_TARGET                                  ("navigate_to_coordinates", null,  NavigateToCoordinatesHandler.class),
    NAVIGATION_ON_OFF                                   ("cancel_navigation", null,  NavigationOnOffHandler.class),
    DISCOVERY_ON_OFF                                    ("toggle_discovery_announcements", null, DiscoveryOnOffHandler.class),
    MINING_ON_OFF                                       ("toggle_mining_announcements", null, MiningOnOffHandler.class),
    ROUTE_ON_OFF                                        ("toggle_route_announcements", null, RouteAnnouncementsOnOffHandler.class),

    INCREASE_SPEED_BY                                   ("set_speed_plus", BINDING_INCREASE_SPEED.getGameBinding(),  SpeedControlHandler.class),
    DECREASE_SPEED_BY                                   ("set_speed_minus", BINDING_DECREASE_SPEED.getGameBinding(),  SpeedControlHandler.class),

    /// Commands that have a specific handler impl (which uses N bindings inside)
    INCREASE_ENGINES_POWER                              ("transfer_power_to_engines", null,  SetPowerToEnginesHandler.class),
    NAVIGATE_TO_NEXT_BIO_SAMPLE                         ("navigate_to_next_organic_codex_entry", null,  NavigateToNextCodexEntry.class),
    INCREASE_SHIELDS_POWER                              ("transfer_power_to_shields", null,  SetPowerToSystemsHandler.class),
    INCREASE_SYSTEMS_POWER                              ("transfer_power_to_ship_systems", null,  SetPowerToSystemsHandler.class),
    INCREASE_WEAPONS_POWER                              ("transfer_power_to_weapons", null,  SetPowerToWeaponsHandler.class),
    RESET_POWER                                         ("equalize_power_settings", null,  ResetPowerSettings.class),

    OPEN_GALAXY_MAP                                     ("open_galaxy_star_map", null,  OpenGalaxyMapHandler.class),
    OPEN_SYSTEM_MAP                                     ("open_local_system_map", null,  OpenSystemMapHandler.class),
    CLOSE_ANY_MAP                                       ("close_map", null,  ExitToHud.class),
    EXIT_TO_HUD                                         ("display_hud", null,  ExitToHud.class),
    EXIT                                                ("exit", null,  ExitToHud.class),

    DISPLAY_COMMS_PANEL                                 ("display_comms_panel", null,  DisplayCommsPanelHandler.class),
    DISPLAY_CONTACTS_PANEL                              ("display_contacts", null,  DisplayContactsPanelHandler.class),
    DISPLAY_INTERNAL_PANEL                              ("display_internal_panel", null,  DisplayInternalPanelHandler.class),
    DISPLAY_STATUS_PANEL                                ("display_status_panel", null,  DisplayStatusPanelHandler.class),
    DISPLAY_RADAR_PANEL                                 ("display_radar_panel", null,  DisplayRadarPanelHandler.class),
    DISPLAY_LOADOUT_PANEL                               ("display_loadout_panel", null,  DisplayLoadoutPanelHandler.class),
    DISPLAY_CARRIER_MANAGEMENT                          ("display_carrier_management", null,  OpenFleetCarrierManagementHandler.class    ),
    OPEN_CARGO_SCOOP                                    ("open_cargo_scoop", null,  OpenCargoScoopHandler.class),
    CLOSE_CARGO_SCOOP                                   ("close_cargo_scoop", null,  CloseCargoScoopHandler.class),
    RETRACT_HARDPOINTS                                  ("retract_hardpoints", null,  RetractHardpointsHandler.class),
    DEPLOY_HARDPOINTS                                   ("deploy_hardpoints", null,  DeployHardpointsHandler.class),
    DEPLOY_LANDING_GEAR                                 ("deploy_landing_gear_down", null,  DeployLandingGearHandler.class),
    RETRACT_LANDING_GEAR                                ("retract_landing_gear_up", null,  RetractLandingGearHandler.class),

    JUMP_TO_HYPERSPACE                                  ("jump_to_hyperspace", null,  JumpToHyperspaceHandler.class),
    ENTER_SUPER_CRUISE                                  ("enter_super_cruise", null,  EnterFtlHandler.class),

    EXIT_SUPER_CRUISE                                   ("drop_from_super_cruise", null,  ExitFtlHandler.class),
    ACTIVATE_ANALYSIS_MODE                              ("swap_to_hud_analysis_mode", null,  ActivateAnalysisModeHandler.class),
    ACTIVATE_COMBAT_MODE                                ("swap_to_hud_combat_mode", null,  ActivateCombatModeHandler.class),
    PLOT_ROUTE_TO_CARRIER                               ("plot_route_to_fleet_carrier", null,  PlotRouteToMyFleetCarrier.class),
    SET_OPTIMAL_SPEED                                   ("set_optimal_speed", null,  SetOptimalSpeedHandler.class),
    TAKE_ME_HOME                                        ("plot_route_to_home_star", null,  PlotRouteToHomeHandler.class),
    OPEN_FSS_AND_SCAN                                   ("open_fss_to_scan_or_honk_star_system", null,  DisplayFssAndScanHandler.class),

    GET_HEADING_TO_LZ                                   ("navigate_bearing_direction_to_landing_zone", null,  NavigateToLandingZone.class),
    DEPLOY_SRV                                          ("deploy_srv", null,  DeploySrvHandler.class),
    RECOVER_SRV                                         ("recover_srv", null,  BoardSrvHandler.class),
    CLEAR_CODEX_ENTRIES                                 ("clear_codex_entries", null,  ClearCodexEntriesHandler.class),
    CALCULATE_FLEET_CARRIER_ROUTE                       ("calculate_fleet_carrier_route", null,  CalculateFleetCarrierRouteHandler.class),
    ENTER_NEXT_FLEET_CARRIER_DESTINATION                ("enter_next_fleet_carrier_destination", null,  EnterNextCarrierDestinationHandler.class),
    SHUT_DOWN                                           ("system_shut_down", null,  SystemShutDownRequestHandler.class),

    CALCULATE_TRADE_ROUTE                               ("calculate_trade_route", null,  CalculateTradeRouteHandler.class),
    PLOT_ROUTE_TO_NEXT_TRADE_STOP                       ("navigate_to_next_trade_stop_port_or_station", null,  PlotRouteToNextTradeStopHandler.class),
    CHANGE_TRADE_PROFILE_SET_STARTING_BUDGET            ("alter_trade_profile_set_starting_budget", null,  ChangeTradeProfileSetStartingBudgetHander.class),
    CHANGE_TRADE_PROFILE_SET_MAX_NUMBER_OF_STOPS        ("alter_trade_profile_set_maximum_number_of_stops", null,  ChangeTradeProfileSetMaxStopsHandler.class),
    CHANGE_TRADE_PROFILE_SET_MAX_DISTANCE_FROM_ENTRY    ("alter_trade_profile_set_maximum_distance_from_entry", null,  ChangeTradeProfileSetMaxDistanceFromEntryHandler.class),
    CHANGE_TRADE_PROFILE_SET_ALLOW_PROHIBITED_CARGO     ("alter_trade_profile_toggle_prohibited_cargo", null, ChangeTradeProfileSetAllowProhibitedCargoHandler.class),
    CHANGE_TRADE_PROFILE_SET_ALLOW_PLANETARY_PORT       ("alter_trade_profile_toggle_planetary_ports", null, ChangeTradeProfileSetIncluidePlanetaryPortsHandler.class),
    CHANGE_TRADE_PROFILE_SET_ALLOW_PERMIT_SYSTEMS       ("alter_trade_profile_toggle_permit_protected_star_systems", null, ChangeTradeProfileAllowPermitSystemsHandler.class),
    CHANGE_TRADE_PROFILE_SET_ALLOW_STRONGHOLDS          ("alter_trade_profile_toggle_strongholds", null, ChangeTradeProfileSetAllowEnemyStrongHoldsHandler.class),
    LIST_TRADE_ROUTE_PARAMETERS                         ("list_available_trade_route_parameters", null,  ListAvailableTradeRouteProfilesHandler.class),
    CLEAR_TRADE_ROUTE                                   ("clear_trade_route", null,  ClearTradeRouteHandler.class),

    /// Generic simple commands. no parameters, but require binding
    ACTIVATE                                            ("activate", BINDING_ACTIVATE.getGameBinding(), GenericGameControlHandler.class),

    REQUEST_DOCKING                                     ("request_docking", null,  RequestDockingHandler.class),

    NIGHT_VISION_ON_OFF                                     ("toggle_night_vision_on_off", BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), ToggleNightVision.class),

    //CYCLE_NEXT_PAGE                                     ("cycle_next_page", BINDING_CYCLE_NEXT_PAGE.getGameBinding(), GenericGameControlHandler.class),
    //CYCLE_NEXT_PANEL                                    ("cycle_next_panel", BINDING_CYCLE_NEXT_PANEL.getGameBinding(), GenericGameControlHandler.class),
    //CYCLE_PREVIOUS_PAGE                                 ("cycle_previous_page", BINDING_CYCLE_PREVIOUS_PAGE.getGameBinding(), GenericGameControlHandler.class),
    //CYCLE_PREVIOUS_PANEL                                ("cycle_previous_panel", BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding(), GenericGameControlHandler.class),
    //CYCLE_NEXT_SUBSYSTEM                                ("cycle_next_subsystem", BINDING_CYCLE_NEXT_SUBSYSTEM.getGameBinding(), GenericGameControlHandler.class),
    //CYCLE_PREVIOUS_SUBSYSTEM                            ("cycle_previous_subsystem", BINDING_CYCLE_PREVIOUS_SUBSYSTEM.getGameBinding(), GenericGameControlHandler.class),
    DEPLOY_HEAT_SINK                                    ("deploy_heat_sink", BINDING_DEPLOY_HEAT_SINK.getGameBinding(), GenericGameControlHandler.class),
    DRIVE_ASSIST                                        ("drive_assist", BINDING_DRIVE_ASSIST.getGameBinding(), GenericGameControlHandler.class),

    TARGET_SUB_SYSTEM                                   ("target_subsystem", null, TargetSubSystemHandler.class),
    RECALL_DISMISS_SHIP                                 ("recall_or_dismiss_ship", BINDING_RECALL_DISMISS_SHIP.getGameBinding(), DismissShip.class),

    REQUEST_DEFENSIVE_BEHAVIOUR                         ("fighter_orders_defend_fighter_order", BINDING_REQUEST_DEFENSIVE_BEHAVIOUR.getGameBinding(), GenericGameControlHandler.class),
    REQUEST_FOCUS_TARGET                                ("fighter_orders_attack_my_target_fighter_order", BINDING_REQUEST_DEFENSIVE_BEHAVIOUR.getGameBinding(), GenericGameControlHandler.class),
    REQUEST_HOLD_FIRE                                   ("fighter_orders_hold_your_fire_fighter_order", BINDING_REQUEST_HOLD_FIRE.getGameBinding(), GenericGameControlHandler.class),
    REQUEST_REQUEST_DOCK                                ("fighter_orders_recall", BINDING_REQUEST_REQUEST_DOCK.getGameBinding(), GenericGameControlHandler.class),
    STOP                                                ("stop", BINDING_SET_SPEED_ZERO.getGameBinding(), GenericGameControlHandler.class),
    SET_SPEED_ZERO                                      ("set_speed_to_zero_0", BINDING_SET_SPEED_ZERO.getGameBinding(), GenericGameControlHandler.class),
    SET_SPEED25                                         ("set_speed_to_slow_throttle_25", BINDING_SET_SPEED25.getGameBinding(), GenericGameControlHandler.class),
    SET_SPEED50                                         ("set_speed_to_medium_throttle_50", BINDING_SET_SPEED50.getGameBinding(), GenericGameControlHandler.class),
    SET_SPEED75                                         ("set_speed_to_optimal_throttle_75", BINDING_SET_SPEED75.getGameBinding(), GenericGameControlHandler.class),
    SET_SPEED100                                        ("set_speed_to_maximum_throttle_100", BINDING_SET_SPEED100.getGameBinding(), GenericGameControlHandler.class),

    SET_CARRIER_FUEL_RESERVE                            ("set_tritium_carrier_fuel_reserve", null,  SetFleetCarrierFuelReserveHandler.class),

    SELECT_HIGHEST_THREAT                               ("target_highest_threat", BINDING_SELECT_HIGHEST_THREAT.getGameBinding(), GenericGameControlHandler.class),
    TARGET_NEXT_ROUTE_SYSTEM                            ("select_next_system_in_route", BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), GenericGameControlHandler.class),

    TARGET_WINGMAN0                                     ("target_wingman_1", BINDING_TARGET_WINGMAN0.getGameBinding(), GenericGameControlHandler.class),
    TARGET_WINGMAN1                                     ("target_wingman_2", BINDING_TARGET_WINGMAN1.getGameBinding(), GenericGameControlHandler.class),
    TARGET_WINGMAN2                                     ("target_wingman_3", BINDING_TARGET_WINGMAN2.getGameBinding(), GenericGameControlHandler.class),
    WING_NAV_LOCK                                       ("lock_on_wingman", BINDING_WING_NAV_LOCK.getGameBinding(), GenericGameControlHandler.class),
    LIST_AVAILABLE_VOICES                               ("list_available_voices", null,  ListAvailableVoices.class),
    ;

    ///
    private final String action;
    private final String binding;
    private final Class<? extends CommandHandler> handlerClass;

    Commands(String action, String binding, Class<? extends CommandHandler> handlerClass) {
        this.action = action;
        this.binding = binding;
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

    public String getBinding() {
        return binding;
    }

    public Class<? extends CommandHandler> getHandlerClass() {
        return handlerClass;
    }
}
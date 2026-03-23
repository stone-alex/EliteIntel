package elite.intel.ai.brain.handlers.commands;

import static elite.intel.ai.brain.commons.AiEndPoint.CONNECTION_CHECK_COMMAND;
import static elite.intel.ai.brain.handlers.commands.Bindings.GameCommand.*;


// @formatter:off
public enum Commands {

    CONNECTION_CHECK                                    (CONNECTION_CHECK_COMMAND, null, ConnectionCheck.class),
    DISABLE_ALL_ANNOUNCEMENTS                           ("disble_all_announcements", null, DisableAllAnnouncementHandler.class),
    NAVIGATE_TO_NEXT_MISSION                            ("navigate_to_active_mission_location",null, PlotRouteToMissionDestination.class),
    LIGHTS_ON_OFF                                       ("toggle_lights_on_off", null, LightsOnOffHandler.class),
    ADD_MINING_TARGET                                   ("add_mining_target", null, AddMiningTargetHandler.class),
    CLEAR_MINING_TARGETS                                ("clear_mining_targets", null, ClearMiningTargetsHandler.class),
    CLEAR_CACHE                                         ("clear_cache", null, ClearCacheHandler.class),
    INTERRUPT_TTS                                       ("shut_up_cancel_interrupt_tts_vocalization", null,ShutUpHandler.class),

    FIND_RAW_MATERIAL_TRADER                            ("find_raw_material_trader", null, FindRawMaterialTraderHandler.class),
    FIND_ENCODED_MATERIAL_TRADER                        ("find_encoded_material_trader", null, FindEncodedMaterialTraderHandler.class),
    FIND_MANUFACTURED_MATERIAL_TRADER                   ("find_manufactured_material_trader", null, FindManufacturedMaterialTraderHandler.class),

    FIND_HUNTING_GROUNDS                                ("search_for_hunting_grounds_for_pirate_massacre_missions", null,  LocatePirateHuntingGrounds.class),
    RECON_TARGET_SYSTEM                                 ("navigate_plot_reconnaissance_route_to_hunting_grounds_target_star_system", null,  ReconPirateMissionTargetSystemHandler.class),
    RECON_PROVIDER_SYSTEM                               ("navigate_to_system_with_matching_mission_provider", null,  ReconMissionProviderSystemHandler.class),
    IGNORE_HUNTING_GROUND                               ("ignore_hunting_ground", null, IgnoreHuntingGroundHandler.class),
    CONFIRM_HUNTING_GROUND                              ("confirm_hunting_ground", null, ConfirmHuntingGroundHandler.class),
    NAVIGATE_TO_PIRATE_MISSION_TARGET_SYSTEM            ("navigate_to_pirate_massacre_mission_target_system", null,  NavigateToPirateMassacreMissionTargetHandler.class),
    NAVIGATE_TO_PIRATE_MISSION_PROVIDER                 ("navigate_to_known_pirate_massacre_mission_provider", null,  PlotRouteToKnownPirateMassacreMissionProvider.class),

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
    SET_AI_VOICE                                        ("set_or_change_voice_to", null,  ChangeShipVoiceHandler.class),
    SET_HOME_SYSTEM                                     ("set_location_as_home_star_system", null,  SetCurrentStarAsHomeSystem.class),
    SET_PERSONALITY                                     ("set_personality", null, SetPersonalityHandler.class),
    SET_CADENCE                                         ("set_cadence", null, SetCadenceHandler.class),
    SET_RADIO_TRANSMISSION_MODE                         ("turn_radio_transmission_on_off", null,  SetRadioTransmissionOnOff.class),
    SET_RADAR_CONTACT_ANNOUNCEMENT                      ("turn_radar_contact_announcements_on_off", null, RadarAnnouncementOnOffHandler.class),

    START_LISTENING                                     ("start_listening_monitor_commands_do_not_ignore_user", null, StartListeningHandler.class),
    STOP_LISTENING                                      ("stop_listening_ignore_commands", null, StopListeningHandler.class),

    NAVIGATE_TO_TARGET                                  ("navigate_to_coordinates", null,  NavigateToCoordinatesHandler.class),
    NAVIGATION_ON_OFF                                   ("cancel_navigation", null,  NavigationOnOffHandler.class),
    DISCOVERY_ON_OFF                                    ("toggle_discovery_announcements", null, DiscoveryOnOffHandler.class),
    MINING_ON_OFF                                       ("toggle_mining_announcements", null, MiningOnOffHandler.class),
    ROUTE_ON_OFF                                        ("toggle_route_announcements", null, RouteAnnouncementsOnOffHandler.class),

    INCREASE_SPEED_BY                                   ("set_speed_plus", BINDING_INCREASE_SPEED.getGameBinding(),  SpeedPlusControlHandler.class),
    DECREASE_SPEED_BY                                   ("set_speed_minus", BINDING_DECREASE_SPEED.getGameBinding(),  SpeedMinusControlHandler.class),

    /// Commands that have a specific handler impl (which uses N bindings inside)
    INCREASE_ENGINES_POWER                              ("transfer_power_to_engines", null,  SetPowerToEnginesHandler.class),
    NAVIGATE_TO_NEXT_BIO_SAMPLE                         ("navigate_to_next_organic_codex_entry", null,  NavigateToNextCodexEntry.class),
    INCREASE_SHIELDS_POWER                              ("transfer_power_to_shields", null,  SetPowerToSystemsHandler.class),
    INCREASE_SYSTEMS_POWER                              ("transfer_power_to_ship_systems", null,  SetPowerToSystemsHandler.class),
    INCREASE_WEAPONS_POWER                              ("transfer_power_to_weapons", null,  SetPowerToWeaponsHandler.class),
    RESET_POWER                                         ("equalize_power_settings", null,  ResetPowerSettings.class),

    OPEN_GALAXY_MAP                                     ("show_open_galaxy_star_map", null,  OpenGalaxyMapHandler.class),
    OPEN_SYSTEM_MAP                                     ("show_open_local_system_map", null,  OpenLocalMapHandler.class),


    ///DISPLAY_INTERNAL_PANEL                              ("display_internal_panel", null,  DisplayInternalPanelHandler.class),
    ///DISPLAY_STATUS_PANEL                                ("display_status_panel", null,  DisplayStatusPanelHandler.class),
    DISPLAY_RADAR_PANEL                                 ("display_radar_panel", null,  DisplayRadarPanelHandler.class),
    ///
    DISPLAY_CARRIER_MANAGEMENT                          ("display_carrier_management", null,  OpenFleetCarrierManagementHandler.class    ),
    OPEN_CARGO_SCOOP                                    ("open_cargo_scoop", null,  OpenCargoScoopHandler.class),
    CLOSE_CARGO_SCOOP                                   ("close_cargo_scoop", null,  CloseCargoScoopHandler.class),
    RETRACT_HARDPOINTS                                  ("retract_hardpoints", null,  RetractHardpointsHandler.class),
    DEPLOY_HARDPOINTS                                   ("deploy_hardpoints", null,  DeployHardpointsHandler.class),
    DEPLOY_LANDING_GEAR                                 ("gear_down", null,  DeployLandingGearHandler.class),
    RETRACT_LANDING_GEAR                                ("gear_up", null,  RetractLandingGearHandler.class),

    JUMP_TO_HYPERSPACE                                  ("jump_to_hyperspace", null,  JumpToHyperspaceHandler.class),
    ENTER_SUPER_CRUISE                                  ("enter_super_cruise", null,  SuperCruiseHandler.class),

    DROP_FROM_SUPER_CRUISE                              ("drop_from_super_cruise", null,  DropFromFtlHandler.class),
    ACTIVATE_ANALYSIS_MODE                              ("swap_to_hud_analysis_mode", null,  ActivateAnalysisModeHandler.class),
    ACTIVATE_COMBAT_MODE                                ("swap_to_hud_combat_mode", null,  ActivateCombatModeHandler.class),
    NAVIGATE_TO_CARRIER                                 ("navigate_to_fleet_carrier", null,  PlotRouteToMyFleetCarrier.class),
    SET_OPTIMAL_SPEED                                   ("set_optimal_approach_speed", null,  SetOptimalSpeedHandler.class),
    TAKE_ME_HOME                                        ("navigate_to_home_star", null,  PlotRouteToHomeHandler.class),
    OPEN_FSS_AND_SCAN                                   ("open_fss_to_scan_or_honk_star_system", null,  DisplayFssAndScanHandler.class),

    GET_HEADING_TO_LZ                                   ("navigate_bearing_direction_to_landing_zone", null,  NavigateToLandingZone.class),
    DEPLOY_SRV                                          ("deploy_srv_car", null,  DeploySrvHandler.class),
    DEPLOY_FIGHTER                                      ("deploy_fighter", null,  DeployFighterHandler.class),
    RECOVER_SRV                                         ("recover_srv_car_board_ship", null,  RecoverSrvHandler.class),
    CLEAR_CODEX_ENTRIES                                 ("clear_codex_entries", null,  ClearCodexEntriesHandler.class),
    CALCULATE_FLEET_CARRIER_ROUTE                       ("calculate_fleet_carrier_route", null,  CalculateFleetCarrierRouteHandler.class),
    ENTER_FLEET_CARRIER_DESTINATION                     ("enter_fleet_carrier_destination", null,  EnterNextCarrierDestinationHandler.class),
    //SHUT_DOWN                                           ("system_shut_down", null,  SystemShutDownRequestHandler.class),

    CALCULATE_TRADE_ROUTE                               ("calculate_trade_route", null,  CalculateTradeRouteHandler.class),
    NAVIGATE_TO_NEXT_TRADE_STOP                       ("navigate_to_trade_stop_port_or_station", null,  PlotRouteToNextTradeStopHandler.class),
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
    SHOW_TRANSACTIONS                                   ("show_transactions", null, OpenTransactionHandler.class),
    SHOW_CONTACTS                                       ("show_contacts", null, OpenContactsHandler.class),
    SHOW_NAVIGATION                                     ("show_navigation", null, OpenNavigationHandler.class),
    SHOW_CHAT_PANEL                                     ("show_chat", null, OpenCommsPanelHandler.class   ),
    SHOW_INBOX_PANEL                                    ("show_inbox", null, OpenInboxPenalHandler.class),
    SHOW_SOCIAL_PANEL                                   ("show_social", null, OpenSocialPanelHandler.class),
    SHOW_HISTORY_PANEL                                  ("show_history", null, OpenHistoryPanelHandler.class),
    SHOW_SQUADRON                                       ("show_squadron", null, OpenSquadronHandler.class),
    SHOW_COMMANDER_PANEL                                ("show_role_panel", null, OpenCommanderPanel.class),
    SHOW_FIGHTER_PANEL                                  ("show_fighter_panel", null, OpenFighterPanelHandler.class),
    SHOW_CREW                                           ("show_crew", null, OpenCrewPanelHandler.class),
    SHOW_INTERNAL_PANEL                                 ("show_internal_panel", null, OpenInternalPanelHandler.class),
    SHOW_MODULES_PANEL                                  ("show_modules_panel", null, OpenModulesPanelHandler.class),
    SHOW_FIRE_GROUPS                                    ("show_fire_groups", null, OpenFireGroupsPanelHandler.class),
    SHOW_INVENTORY_PANEL                                ("show_inventory_panel", null, OpenInventoryHandler.class),
    SHOW_STORAGE_PANEL                                  ("show_storage_panel", null, OpenStoragePanelHandler.class),
    SHOW_STATUS_PANEL                                   ("show_status_panel", null, OpenStatusPanelHandler.class),
    SHOW_SHIP_PANEL                                     ("show_ship_panel", null, OpenShipPanelHandler.class),
    EXIT_CLOSE                                          ("exit_close", null, ClosePanelHandler.class),


    REQUEST_DOCKING                                     ("request_docking", null,  RequestDockingHandler.class),
    NIGHT_VISION_ON_OFF                                 ("toggle_night_vision_on_off", BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), ToggleNightVision.class),

    CYCLE_NEXT_PAGE                                     ("cycle_next_page", BINDING_CYCLE_NEXT_PAGE.getGameBinding(), GenericGameControlHandler.class),
    CYCLE_NEXT_PANEL                                    ("cycle_next_panel", BINDING_CYCLE_NEXT_PANEL.getGameBinding(), GenericGameControlHandler.class),
    CYCLE_PREVIOUS_PAGE                                 ("cycle_previous_page", BINDING_CYCLE_PREVIOUS_PAGE.getGameBinding(), GenericGameControlHandler.class),
    CYCLE_PREVIOUS_PANEL                                ("cycle_previous_panel", BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding(), GenericGameControlHandler.class),
    CYCLE_NEXT_SUBSYSTEM                                ("cycle_next_subsystem", BINDING_CYCLE_NEXT_SUBSYSTEM.getGameBinding(), GenericGameControlHandler.class),
    CYCLE_PREVIOUS_SUBSYSTEM                            ("cycle_previous_subsystem", BINDING_CYCLE_PREVIOUS_SUBSYSTEM.getGameBinding(), GenericGameControlHandler.class),

    DEPLOY_HEAT_SINK                                    ("deploy_heat_sink", BINDING_DEPLOY_HEAT_SINK.getGameBinding(), GenericGameControlHandler.class),
    DRIVE_ASSIST                                        ("drive_assist", BINDING_DRIVE_ASSIST.getGameBinding(), GenericGameControlHandler.class),

    TARGET_SUB_SYSTEM                                   ("target_subsystem", null, TargetSubSystemHandler.class),
    DISMISS_SHIP                                        ("dismiss_ship_go_to_orbit", BINDING_RECALL_DISMISS_SHIP.getGameBinding(), DismissRecallShip.class),
    RETURN_TO_SURFACE                                   ("recall_ship_pick_me_up_return_to_surface", BINDING_RECALL_DISMISS_SHIP.getGameBinding(), DismissRecallShip.class),

    FIGHTER_REQUEST_DEFENSIVE_BEHAVIOUR                         ("fighter_orders_defend_fighter_order", BINDING_REQUEST_DEFENSIVE_BEHAVIOUR.getGameBinding(), GenericGameControlHandler.class),
    FIGHTER_REQUEST_FOCUS_TARGET                                ("fighter_orders_attack_my_target_fighter_order", BINDING_REQUEST_DEFENSIVE_BEHAVIOUR.getGameBinding(), GenericGameControlHandler.class),
    FIGHTER_REQUEST_HOLD_FIRE                                   ("fighter_orders_hold_your_fire_fighter_order", BINDING_REQUEST_HOLD_FIRE.getGameBinding(), GenericGameControlHandler.class),
    FIGHTER_REQUEST_REQUEST_DOCK                                ("fighter_orders_recall", BINDING_REQUEST_REQUEST_DOCK.getGameBinding(), GenericGameControlHandler.class),
    STOP                                                ("stop", BINDING_SET_SPEED_ZERO.getGameBinding(), GenericGameControlHandler.class),
    TAXI                                                ("taxi", BINDING_SET_SPEED_ZERO.getGameBinding(), GenericGameControlHandler.class),
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
    CLEAR_REMINDERS                                     ("clear_reminders", null, CleareReminderHandler.class),
    SET_REMINDER                                        ("set_reminder", null, SetReminderHandler.class),
    DELETE_CODEX_ENTRY                                  ("delete_codex_entry", null, DeleteCodexEntryHandler.class),
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
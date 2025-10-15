package elite.intel.ai.brain.handlers.commands;

import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

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
public enum Commands {


    /// Commands the require parameters
    LIGHTS_ON_OFF("toggle_lights", null, "lights", LightsOnOffHandler.class),
    ADD_MINING_TARGET("add_mining_target", null, "mining_target", AddMiningTargetHandler.class),
    CLEAR_MINING_TARGETS("clear_mining_targets",  null, null, ClearMiningTargetsHandler.class),
    CLEAR_CACHE("clear_cache", null,  "session_clear", ClearCacheHandler.class),
    FIND_COMMODITY("find_market_where_to_buy", null, "commodity", FindCommodityHandler.class),
    SET_AI_VOICE("set_or_change_voice_to", null, "voice_name", ChangeAiVoiceHandler.class),
    SET_HOME_SYSTEM("set_location_as_home_star_system", null,  null, SetCurrentStarAsHomeSystem.class),
    SET_PERSONALITY("set_personality", null, PlayerSession.PERSONALITY, SetPersonalityHandler.class),
    SET_PROFILE("set_profile", null,  PlayerSession.PROFILE, SetCadenceHandler.class),
    SET_RADIO_TRANSMISSION_MODDE("turn_radio_transmission_on_off", null, SystemSession.RADION_TRANSMISSION_ON_OFF, SetRadioTransmissionOnOff.class),
    SET_STREAMING_MODE("set_streaming_mode", null,  "streaming_mode", SetStreamingModeHandler.class),
    NAVIGATE_TO_TARGET("navigate_to_coordinates", null,  "lat_lon", NavigateToCoordinatesHandler.class),
    NAVIGATION_ON_OFF("toggle_navigator_guidance",  null,  "navigation_on_off", NavigationOnOffHandler.class),
    DISCOVERY_ON_OFF("toggle_discovery_announcements",  null, "discovery_on_off", DiscoveryOnOffHandler.class),
    MINING_ON_OFF("toggle_mining_announcements",  null,  "mining_on_off", MiningOnOffHandler.class),
    ROUTE_ON_OFF("toggle_route_announcements",  null,  "route_on_off", RouteAnnouncementsOnOffHandler.class),
    HELP("help_with_topic",  null, "help_topic", HelpHandler.class),

    INCREASE_SPEED_BY("speed_plus", BINDING_INCREASE_SPEED.getGameBinding(), "value", SpeedControlHandler.class),
    DECREASE_SPEED_BY("speed_minus", BINDING_DECREASE_SPEED.getGameBinding(), "value", SpeedControlHandler.class),


    /// Commands that have a specific handler impl (which uses N bindings inside)
    INCREASE_ENGINES_POWER("transfer_power_to_engines", null, null,  SetPowerToEnginesHandler.class),
    NAVIGATE_TO_NEXT_BIO_SAMPLE("navigate_directions_to_codex_entry_or_bio_sample", null, null,  NavigateToNextBioSample.class),
    INCREASE_SHIELDS_POWER("transfer_power_to_shields", null, null,  SetPowerToSystemsHandler.class),
    INCREASE_SYSTEMS_POWER("transfer_power_to_ship_systems", null, null,  SetPowerToSystemsHandler.class),
    INCREASE_WEAPONS_POWER("transfer_power_to_weapons", null, null,  SetPowerToWeaponsHandler.class),
    RESET_POWER("reset_power_settings", null, null,  ResetPowerSettings.class),

    OPEN_GALAXY_MAP("open_galaxy_map", null, null,  OpenGalaxyMapHandler.class),
    OPEN_SYSTEM_MAP("open_local_map", null, null,  OpenSystemMapHandler.class),
    CLOSE_ANY_MAP("close_map", null, null,  ExitToHud.class),
    EXIT_TO_HUD("display_hud", null, null,  ExitToHud.class),
    EXIT("exit", null, null,  ExitToHud.class),

    DISPLAY_COMMS_PANEL("display_comms_panel", null, null,  DisplayCommsPanelHandler.class),
    DISPLAY_CONTACTS_PANEL("display_contacts", null, null,  DisplayContactsPanelHandler.class),
    DISPLAY_LEFT_PANEL("display_navigation_panel", null, null,  DisplayNavigationPanelHandler.class),
    DISPLAY_INTERNAL_PANEL("display_internal_panel", null, null,  DisplayInternalPanelHandler.class),
    DISPLAY_STATUS_PANEL("display_status_panel", null, null,  DisplayStatusPanelHandler.class),
    DISPLAY_RADAR_PANEL("display_radar_panel", null, null,  DisplayRadarPanelHandler.class),
    DISPLAY_LOADOUT_PANEL("display_loadout_panel", null, null,  DisplayLoadoutPanelHandler.class),

    /// might not be a good idea..!
    EJECT_ALL_CARGO("eject_all_cargo", null, null,  EjectAllCargoHandler.class),
    GALAXY_MAP("open_galaxy_star_map", null, null,  OpenGalaxyMapHandler.class),
    LOCAL_MAP("open_local_map", null, null,  OpenLocalMapHandler.class),
    OPEN_CARGO_SCOOP("open_cargo_scoop", null, null,  OpenCargoScoopHandler.class),
    CLOSE_CARGO_SCOOP("close_cargo_scoop", null, null,  CloseCargoScoopHandler.class),
    RETRACT_HARDPOINTS("retract_hardpoints", null, null,  RetractHardpointsHandler.class),
    DEPLOY_HARDPOINTS("deploy_hardpoints", null, null,  DeployHardpointsHandler.class),
    WEAPONS_HOT("weapons_hot", null, null,  DeployHardpointsHandler.class),
    DEPLOY_LANDING_GEAR("deploy_landing_gear", null, null,  DeployLandingGearHandler.class),
    RETRACT_LANDING_GEAR("retract_landing_gear", null, null,  RetractLandingGearHandler.class),

    //Hyperspace - direct. will fail if target is obscured.
    JUMP_TO_HYPERSPACE("jump_to_hyperspace", null, null,  JumpToHyperspaceHandler.class),
    //smarter commands, automatically determine supercruise or ftl.
    GET_OUT_OF_HERE("lets_get_out_of_here", null, null,  EnterFtlHandler.class),
    LETS_GO("lets_go", null, null,  EnterFtlHandler.class),
    ENTER_SUPER_CRUISE("enter_super_cruise", null, null,  EnterFtlHandler.class),
    //exit supercruise.,
    EXIT_SUPER_CRUISE("exit_super_cruise", null, null,  ExitFtlHandler.class),
    ACTIVATE_ANALYSIS_MODE("activate_hud_analysis_mode", null, null,  ActivateAnalysisModeHandler.class),
    ACTIVATE_COMBAT_MODE("activate_hud_combat_mode", null, null,  ActivateCombatModeHandler.class),
    PLOT_ROUTE_TO_CARRIER("plot_route_to_fleet_carrier", null, null,  PlotRouteToMyFleetCarrier.class),
    PLOT_ROUTE_TO_BEST_MARKET("plot_route_to_best_market", null, null,  PlotRouteToBestMarketHandler.class),
    SET_OPTIMAL_SPEED("set_optimal_speed", null, null,  SetOptimalSpeedHandler.class),
    TAKE_ME_HOME("take_us_home", null, null,  PlotRouteToHomeHandler.class),
    OPEN_FSS_AND_SCAN("open_fss_and_perform_scan", null, null,  DisplayFssAndScanHandler.class),
    HONK("perform_honk", null, null,  DisplayFssAndScanHandler.class),

    GET_HEADING_TO_LZ("navigate_bearing_direction_to_landing_zone", null, null,  NavigateToLandingZone.class),
    DEPLOY_SRV("deploy_srv", null, null,  DeploySrvHandler.class),
    BOARD_SRV("get_on_board_srv", null, null,  BoardSrvHandler.class),
    CLEAR_CODEX_ENTRIES("clear_codex_entries", null, null,  ClearCodexEntriesHandler.class),
    CALCULATE_FLEET_CARRIER_ROUTE("calculate_fleet_carrier_route", null, null,  CalculateFleetCarrierRouteHandler.class),
    ENTER_NEXT_FLEET_CARRIER_DESTINATION("enter_next_fleet_carrier_destination", null, null,  EnterNextCarrierDestinationHandler.class),
    LIST_HELP_TOPICS("list_help_topics", null, null,  HelpHandler.class),
    SHUT_DOWN("system_shut_down", null, null,  SystemShutDownRequestHandler.class),



    /// Generic simple commands. no parameters, but require binding
    ACTIVATE("activate", BINDING_ACTIVATE.getGameBinding(), null, GenericGameControlHandler.class),
    NIGHT_VISION_ON("activate_night_vision", BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), null, GenericGameControlHandler.class),
    NIGHT_VISION_OFF("deactivate_night_vision", BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), null, GenericGameControlHandler.class),
    CYCLE_NEXT_PAGE("cycle_next_page", BINDING_CYCLE_NEXT_PAGE.getGameBinding(), null, GenericGameControlHandler.class),
    CYCLE_NEXT_PANEL("cycle_next_panel", BINDING_CYCLE_NEXT_PANEL.getGameBinding(), null, GenericGameControlHandler.class),
    CYCLE_PREVIOUS_PAGE("cycle_previous_page", BINDING_CYCLE_PREVIOUS_PAGE.getGameBinding(), null, GenericGameControlHandler.class),
    CYCLE_PREVIOUS_PANEL("cycle_previous_panel", BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding(), null, GenericGameControlHandler.class),
    CYCLE_NEXT_SUBSYSTEM("cycle_next_subsystem", BINDING_CYCLE_NEXT_SUBSYSTEM.getGameBinding(), null, GenericGameControlHandler.class),
    CYCLE_PREVIOUS_SUBSYSTEM("cycle_previous_subsystem", BINDING_CYCLE_PREVIOUS_SUBSYSTEM.getGameBinding(), null, GenericGameControlHandler.class),
    DEPLOY_HEAT_SINK("deploy_heat_sink", BINDING_DEPLOY_HEAT_SINK.getGameBinding(), null, GenericGameControlHandler.class),
    DRIVE_ASSIST("drive_assist", BINDING_DRIVE_ASSIST.getGameBinding(), null, GenericGameControlHandler.class),
    EXPLORATION_FSSQUIT("exit_fss", BINDING_EXPLORATION_FSSQUIT.getGameBinding(), null, GenericGameControlHandler.class),

    RADAR_DECREASE_RANGE("decrease_radar_range", BINDING_RADAR_DECREASE_RANGE.getGameBinding(), null, GenericGameControlHandler.class),
    RADAR_INCREASE_RANGE("increase_radar_range", BINDING_RADAR_INCREASE_RANGE.getGameBinding(), null, GenericGameControlHandler.class),

    RECALL_DISMISS_SHIP("recall_or_dismiss_ship", BINDING_RECALL_DISMISS_SHIP.getGameBinding(), null, GenericGameControlHandler.class),

    REQUEST_DEFENSIVE_BEHAVIOUR("fighter_orders_defend_fighter_order", BINDING_REQUEST_DEFENSIVE_BEHAVIOUR.getGameBinding(), null, GenericGameControlHandler.class),
    REQUEST_FOCUS_TARGET("fighter_orders_attack_my_target_fighter_order", BINDING_REQUEST_DEFENSIVE_BEHAVIOUR.getGameBinding(), null, GenericGameControlHandler.class),
    REQUEST_HOLD_FIRE("fighter_orders_hold_your_fire_fighter_order", BINDING_REQUEST_HOLD_FIRE.getGameBinding(), null, GenericGameControlHandler.class),
    REQUEST_REQUEST_DOCK("fighter_orders_recall", BINDING_REQUEST_REQUEST_DOCK.getGameBinding(), null, GenericGameControlHandler.class),

    SET_SPEED25("set_speed_to_slow_throttle_25", BINDING_SET_SPEED25.getGameBinding(), null, GenericGameControlHandler.class),
    SET_SPEED50("set_speed_to_medium_throttle_50", BINDING_SET_SPEED50.getGameBinding(), null, GenericGameControlHandler.class),
    SET_SPEED75("set_speed_to_optimal_throttle_75", BINDING_SET_SPEED75.getGameBinding(), null, GenericGameControlHandler.class),
    SET_SPEED100("set_speed_to_maximum_throttle_100", BINDING_SET_SPEED100.getGameBinding(), null, GenericGameControlHandler.class),
    SET_SPEED_ZERO("set_speed_to_zero_0", BINDING_SET_SPEED_ZERO.getGameBinding(), null, GenericGameControlHandler.class),
    STOP("stop", BINDING_SET_SPEED_ZERO.getGameBinding(), null, GenericGameControlHandler.class),
    TAXI("taxi", BINDING_SET_SPEED_ZERO.getGameBinding(), null, GenericGameControlHandler.class),


    SELECT_HIGHEST_THREAT("target_highest_threat", BINDING_SELECT_HIGHEST_THREAT.getGameBinding(), null, GenericGameControlHandler.class),
    TARGET_NEXT_ROUTE_SYSTEM("select_or_target_next_system_in_route", BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), null, GenericGameControlHandler.class),

    TARGET_WINGMAN0("target_wingman1", BINDING_TARGET_WINGMAN0.getGameBinding(), null, GenericGameControlHandler.class),
    TARGET_WINGMAN1("target_wingman2", BINDING_TARGET_WINGMAN1.getGameBinding(), null, GenericGameControlHandler.class),
    TARGET_WINGMAN2("target_wingman3", BINDING_TARGET_WINGMAN2.getGameBinding(), null, GenericGameControlHandler.class),
    WING_NAV_LOCK("lock_on_wingman", BINDING_WING_NAV_LOCK.getGameBinding(), null, GenericGameControlHandler.class);

    ///
    private final String action;
    private final String binding;
    private final String paramKey;
    private final Class<? extends CommandHandler> handlerClass;

    Commands(String action, String binding,  String paramKey, Class<? extends CommandHandler> handlerClass) {
        this.action = action;
        this.binding = binding;
        this.paramKey = paramKey;
        this.handlerClass = handlerClass;
    }

    public static String[] getCustomCommands() {
        String[] result = new String[Commands.values().length];
        for (int i = 0; i < Commands.values().length; i++) {
            result[i] = Commands.values()[i].getAction();
        }
        return result;
    }

    public String getAction() {
        return action;
    }


    public String getParamKey() {
        return paramKey;
    }

    public String getBinding() {
        return binding;
    }

    public Class<? extends CommandHandler> getHandlerClass() {
        return handlerClass;
    }

    public static String getGameBinding(String action) {
        Commands[] values = values();
        for(Commands command : values) {
            if(action.equalsIgnoreCase(command.getAction())){
                return command.getBinding();
            }
        }
        return null;
    }
}
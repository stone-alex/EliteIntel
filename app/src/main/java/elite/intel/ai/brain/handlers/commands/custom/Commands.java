package elite.intel.ai.brain.handlers.commands.custom;

import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

import static elite.intel.ai.brain.handlers.commands.ControllerBindings.GameCommand.*;

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
    ADD_MINING_TARGET("add_mining_target", null, "<material>", "mining_target", AddMiningTargetHandler.class),
    CLEAR_MINING_TARGETS("clear_mining_targets", null, null, null, ClearMiningTargetsHandler.class),
    CLEAR_CACHE("clear_cache", null, "<session_clear>", "session_clear", ClearCacheHandler.class),
    FIND_COMMODITY("find_where_to_buy", null, "<commodity>", "commodity_target", FindCommodityHandler.class),
    SET_AI_VOICE("set_or_change_voice_to", null, "<voice_name>", "voice_name", SetAiVoice.class),
    SET_HOME_SYSTEM("set_location_as_home_star_system", null, null, null, SetCurrentStarAsHomeSystem.class),
    SET_PERSONALITY("set_personality", null, "<personality>", PlayerSession.PERSONALITY, SetPersonalityHandler.class),
    SET_PROFILE("set_profile", null, "<profile>", PlayerSession.PROFILE, SetCadenceHandler.class),
    SET_RADIO_TRANSMISSION_MODDE("turn_radio_transmission_on_off", null, "<radio_transmission_on_off>", SystemSession.RADION_TRANSMISSION_ON_OFF, SetRadioTransmissionOnOff.class),
    SET_STREAMING_MODE("set_streaming_mode", null, "<streaming_mode_on_off>", "streaming_mode", SetStreamingModeHandler.class),
    NAVIGATE_TO_TARGET("navigate_to_coordinates", null, "<coordinates>", "lat_lon", NavigateToCoordinatesHandler.class),
    NAVIGATION_ON_OFF("toggle_navigator_guidance",  null, "<navigation_on_off>", "navigation_on_off", NavigationOnOffHandler.class),
    DISCOVERY_ON_OFF("toggle_discovery_announcements",  null, "<discovery_on_off>", "discovery_on_off", DiscoveryOnOffHandler.class),
    MINING_ON_OFF("toggle_mining_announcements",  null, "<mining_on_off>", "mining_on_off", MiningOnOffHandler.class),
    ROUTE_ON_OFF("toggle_route_announcements",  null, "<route_on_off>", "route_on_off", RouteAnnouncementsOnOffHandler.class),
    HELP("help_with_topic",  null, "<help_topic>", "help_topic", HelpHandler.class),


    /// Commands that have specific handler impl (which uses N bindings inside)
    INCREASE_ENGINES_POWER("all_power_to_engines", null, null, null, SetPowerToEnginesHandler.class),
    NAVIGATE_TO_NEXT_BIO_SAMPLE("navigate_directions_to_codex_entry_or_bio_sample", null, null, null, NavigateToNextBioSample.class),
    INCREASE_SHIELDS_POWER("all_power_to_shields", null, null, null, SetPowerToSystemsHandler.class),
    INCREASE_SYSTEMS_POWER("all_power_to_systems", null, null, null, SetPowerToSystemsHandler.class),
    INCREASE_WEAPONS_POWER("all_power_to_weapons", null, null, null, SetPowerToWeaponsHandler.class),
    RESET_POWER("reset_power_settings", null, null, null, ResetPowerSettings.class),

    OPEN_GALAXY_MAP("open_galaxy_map", null, null, null, OpenGalaxyMapHandler.class),
    OPEN_SYSTEM_MAP("open_local_map", null, null, null, OpenSystemMapHandler.class),
    CLOSE_ANY_MAP("close_map", null, null, null, ExitToHud.class),
    EXIT_TO_HUD("display_hud", null, null, null, ExitToHud.class),

    DISPLAY_COMMS_PANEL("display_comms_panel", null, null, null, DisplayCommsPanelHandler.class),
    DISPLAY_CONTACTS_PANEL("display_contacts", null, null, null, DisplayContactsPanelHandler.class),
    DISPLAY_LEFT_PANEL("display_navigation_panel", null, null, null, DisplayNavigationPanelHandler.class),
    DISPLAY_INTERNAL_PANEL("display_internal_panel", null, null, null, DisplayInternalPanelHandler.class),
    DISPLAY_STATUS_PANEL("display_status_panel", null, null, null, DisplayStatusPanelHandler.class),
    DISPLAY_RADAR_PANEL("display_radar_panel", null, null, null, DisplayRadarPanelHandler.class),
    DISPLAY_LOADOUT_PANEL("display_loadout_panel", null, null, null, DisplayLoadoutPanelHandler.class),

    /// might not be a good idea..!
    EJECT_ALL_CARGO("eject_all_cargo", null, null, null, EjectAllCargoHandler.class),
    GALAXY_MAP("open_galaxy_star_map", null, null, null, OpenGalaxyMapHandler.class),
    LOCAL_MAP("open_local_map", null, null, null, OpenLocalMapHandler.class),
    OPEN_CARGO_SCOOP("open_cargo_scoop", null, null, null, OpenCargoScoopHandler.class),
    CLOSE_CARGO_SCOOP("close_cargo_scoop", null, null, null, CloseCargoScoopHandler.class),
    RETRACT_HARDPOINTS("retract_hardpoints", null, null, null, RetractHardpointsHandler.class),
    DEPLOY_HARDPOINTS("deploy_hardpoints", null, null, null, DeployHardpointsHandler.class),
    WEAPONS_HOT("weapons_hot", null, null, null, DeployHardpointsHandler.class),
    DEPLOY_LANDING_GEAR("deploy_landing_gear", null, null, null, DeployLandingGearHandler.class),
    RETRACT_LANDING_GEAR("retract_landing_gear", null, null, null, RetractLandingGearHandler.class),

    //Hyperspace - direct. will fail if target is obscured.
    JUMP_TO_HYPERSPACE("jump_to_hyperspace", null, null, null, JumpToHyperspaceHandler.class),
    //smarter commands, automatically determine supercruise or ftl.
    GET_OUT_OF_HERE("lets_get_out_of_here", null, null, null, EnterFtlHandler.class),
    LETS_GO("lets_go", null, null, null, EnterFtlHandler.class),
    ENTER_SUPER_CRUISE("enter_super_cruise", null, null, null, EnterFtlHandler.class),
    //exit supercruise.,
    EXIT_SUPER_CRUISE("exit_super_cruise", null, null, null, ExitFtlHandler.class),
    ACTIVATE_ANALYSIS_MODE("activate_hud_analysis_mode", null, null, null, ActivateAnalysisModeHandler.class),
    ACTIVATE_COMBAT_MODE("activate_hud_combat_mode", null, null, null, ActivateCombatModeHandler.class),
    PLOT_ROUTE_TO_CARRIER("plot_route_to_fleet_carrier", null, null, null, PlotRouteToMyFleetCarrier.class),
    PLOT_ROUTE_TO_BEST_MARKET("plot_route_to_best_market", null, null, null, PlotRouteToBestMarketHandler.class),
    SET_OPTIMAL_SPEED("set_optimal_speed", null, null, null, SetOptimalSpeedHandler.class),
    TAKE_ME_HOME("take_us_home", null, null, null, PlotRouteToHomeHandler.class),
    OPEN_FSS_AND_SCAN("open_fss_and_perform_scan", null, null, null, DisplayFssAndScanHandler.class),
    HONK("perform_honk", null, null, null, DisplayFssAndScanHandler.class),

    GET_HEADING_TO_LZ("navigate_bearing_direction_to_landing_zone", null, null, null, NavigateToLandingZone.class),
    DEPLOY_SRV("deploy_srv", null, null, null, DeploySrvHandler.class),
    CLEAR_CODEX_ENTRIES("clear_codex_entries", null, null, null, ClearCodexEntriesHandler.class),
    CALCULATE_FLEET_CARRIER_ROUTE("calculate_fleet_carrier_route", null, null, null, CalculateFleetCarrierRouteHandler.class),
    ENTER_NEXT_FLEET_CARRIER_DESTINATION("enter_next_fleet_carrier_destination", null, null, null, EnterNextCarrierDestinationHandler.class),
    LIST_HELP_TOPICS("list_help_topics", null, null, null, HelpHandler.class),
    SHUT_DOWN("system_shut_down", null, null, null, SystemShutDownRequestHandler.class),


    /// Generic simple commands. no parameters, but require binding
    ACTIVATE("activate", BINDING_ACTIVATE.getGameBinding(), null, null, GenericGameControllHandler.class),
    NIGHT_VISION_ON("activate_night_vision", BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), null, null, GenericGameControllHandler.class),
    NIGHT_VISION_OFF("deactivate_night_vision", BINDING_NIGHT_VISION_TOGGLE.getGameBinding(), null, null, GenericGameControllHandler.class),
    CYCLE_NEXT_PAGE("cycle_next_page", BINDING_CYCLE_NEXT_PAGE.getGameBinding(), null, null, GenericGameControllHandler.class),
    CYCLE_NEXT_PANEL("cycle_next_panel", BINDING_CYCLE_NEXT_PANEL.getGameBinding(), null, null, GenericGameControllHandler.class),
    CYCLE_PREVIOUS_PAGE("cycle_previous_page", BINDING_CYCLE_PREVIOUS_PAGE.getGameBinding(), null, null, GenericGameControllHandler.class),
    CYCLE_PREVIOUS_PANEL("cycle_previous_panel", BINDING_CYCLE_PREVIOUS_PANEL.getGameBinding(), null, null, GenericGameControllHandler.class),
    CYCLE_NEXT_SUBSYSTEM("cycle_next_subsystem", BINDING_CYCLE_NEXT_SUBSYSTEM.getGameBinding(), null, null, GenericGameControllHandler.class),
    CYCLE_PREVIOUS_SUBSYSTEM("cycle_previous_subsystem", BINDING_CYCLE_PREVIOUS_SUBSYSTEM.getGameBinding(), null, null, GenericGameControllHandler.class),
    DEPLOY_HEAT_SINK("deploy_heat_sink", BINDING_DEPLOY_HEAT_SINK.getGameBinding(), null, null, GenericGameControllHandler.class),
    DRIVE_ASSIST("drive_assist", BINDING_DRIVE_ASSIST.getGameBinding(), null, null, GenericGameControllHandler.class),
    EXPLORATION_FSSQUIT("exit_fss", BINDING_EXPLORATION_FSSQUIT.getGameBinding(), null, null, GenericGameControllHandler.class),

    RADAR_DECREASE_RANGE("radar_decrease_range", BINDING_RADAR_DECREASE_RANGE.getGameBinding(), null, null, GenericGameControllHandler.class),
    RADAR_INCREASE_RANGE("radar_increase_range", BINDING_RADAR_INCREASE_RANGE.getGameBinding(), null, null, GenericGameControllHandler.class),

    RECALL_DISMISS_SHIP("recall_or_dismiss_ship", BINDING_RECALL_DISMISS_SHIP.getGameBinding(), null, null, GenericGameControllHandler.class),

    REQUEST_DEFENSIVE_BEHAVIOUR("defend_ship_fighter_order", BINDING_REQUEST_DEFENSIVE_BEHAVIOUR.getGameBinding(), null, null, GenericGameControllHandler.class),
    REQUEST_FOCUS_TARGET("attack_my_target_fighter_order", BINDING_REQUEST_DEFENSIVE_BEHAVIOUR.getGameBinding(), null, null, GenericGameControllHandler.class),
    REQUEST_HOLD_FIRE("hold_your_fire_fighter_order", BINDING_REQUEST_HOLD_FIRE.getGameBinding(), null, null, GenericGameControllHandler.class),
    REQUEST_REQUEST_DOCK("fighter_recall", BINDING_REQUEST_REQUEST_DOCK.getGameBinding(), null, null, GenericGameControllHandler.class),

    SET_SPEED25("set_speed_to_slow_throttle_25", BINDING_SET_SPEED25.getGameBinding(), null, null, GenericGameControllHandler.class),
    SET_SPEED50("set_speed_to_medium_throttle_50", BINDING_SET_SPEED50.getGameBinding(), null, null, GenericGameControllHandler.class),
    SET_SPEED75("set_speed_to_optimal_throttle_75", BINDING_SET_SPEED75.getGameBinding(),  null, null, GenericGameControllHandler.class),
    SET_SPEED100("set_speed_to_maximum_throttle_100", BINDING_SET_SPEED100.getGameBinding(), null, null, GenericGameControllHandler.class),
    SET_SPEED_ZERO("set_speed_to_zero_0", BINDING_SET_SPEED100.getGameBinding(), null, null, GenericGameControllHandler.class),


    SELECT_HIGHEST_THREAT("target_highest_threat", BINDING_SELECT_HIGHEST_THREAT.getGameBinding(), null, null, GenericGameControllHandler.class),
    TARGET_NEXT_ROUTE_SYSTEM("select_or_target_next_system_in_route", BINDING_TARGET_NEXT_ROUTE_SYSTEM.getGameBinding(), null, null, GenericGameControllHandler.class),

    TARGET_WINGMAN0("target_wingman1", BINDING_TARGET_WINGMAN0.getGameBinding(), null, null, GenericGameControllHandler.class),
    TARGET_WINGMAN1("target_wingman2", BINDING_TARGET_WINGMAN1.getGameBinding(), null, null, GenericGameControllHandler.class),
    TARGET_WINGMAN2("target_wingman3", BINDING_TARGET_WINGMAN2.getGameBinding(), null, null, GenericGameControllHandler.class),
    WING_NAV_LOCK("lock_on_wingman", BINDING_WING_NAV_LOCK.getGameBinding(), null, null, GenericGameControllHandler.class);

    ///
    private final String action;
    private final String binding;
    private final String placeholder;
    private final String paramKey;
    private final Class<? extends CommandHandler> handlerClass;

    Commands(String action, String binding, String placeholder, String paramKey, Class<? extends CommandHandler> handlerClass) {
        this.action = action;
        this.binding = binding;
        this.placeholder = placeholder;
        this.paramKey = paramKey;
        this.handlerClass = handlerClass;
    }

    public static String[] getCustomCommands() {
        String[] result = new String[Commands.values().length];
        for (int i = 0; i < Commands.values().length; i++) {
            // These commands used in custom handlers. exclude from generic command handler

            result[i] = Commands.values()[i].getAction();
        }
        return result;

    }

    public String getAction() {
        return action;
    }

    public String getPlaceholder() {
        return placeholder;
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

    public String getCommandWithPlaceholder() {
        return placeholder != null ? action + " " + placeholder : action;
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
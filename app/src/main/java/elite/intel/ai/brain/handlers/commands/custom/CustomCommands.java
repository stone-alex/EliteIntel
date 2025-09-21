package elite.intel.ai.brain.handlers.commands.custom;

import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;

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
public enum CustomCommands {
    ADD_MINING_TARGET("add_mining_target", "<material>", "mining_target", AddMiningTargetHandler.class),
    CLEAR_MINING_TARGETS("clear_mining_targets", null, null, ClearMiningTargetsHandler.class),
    SET_STREAMING_MODE("set_streaming_mode", "<streaming_mode_on_off>", "streaming_mode", SetStreamingModeHandler.class),
    SET_RADIO_TRANSMISSION_MODDE("turn_radio_transmission_on_off", "<radio_transmission_on_off>", SystemSession.RADION_TRANSMISSION_ON_OFF, SetRadioTransmissionOnOff.class),
    CLEAR_SESSION("clear_session", "<session_clear>", "session_clear", ClearSessionHandler.class),

    SET_AI_VOICE("set_or_change_voice_to", "<voice_name>", "voice_name", SetAiVoice.class),
    SET_PROFILE("set_profile", "<profile>", PlayerSession.PROFILE, SetCadenceHandler.class),
    SET_PERSONALITY("set_personality", "<personality>", PlayerSession.PERSONALITY, SetPersonalityHandler.class),

    INCREASE_ENGINES_POWER("power_to_engines", "<power>", "power_to_engines", SetPowerToEnginesHandler.class),
    INCREASE_SYSTEMS_POWER("power_to_systems", "<power>", "power_to_systems", SetPowerToSystemsHandler.class),
    INCREASE_SHIELDS_POWER("power_to_shields", "<power>", "power_to_shields", SetPowerToSystemsHandler.class),//same as systems, handles different phrasing
    INCREASE_WEAPONS_POWER("power_to_weapons", "<power>", "power_to_weapons", SetPowerToWeaponsHandler.class),

    FSSDISCOVERY_SCAN("exploration_fssdiscovery_scan", "ExplorationFSSDiscoveryScan", "<custom>", PerformFSSScanHandler.class),
    SET_OPTIMAL_SPEED("set_optimal_speed", null, null, SetOptimalSpeedHandler.class),
    OPEN_GALAXY_MAP("open_galaxy_map", null, null, OpenGalaxyMapHandler.class),
    OPEN_SYSTEM_MAP("open_local_starsystem_map", null, null, OpenSystemMapHandler.class),

    OPEN_FSS_AND_SCAN("open_fss_and_scan", null, null, DisplayFssAndScanHandler.class),
    OPEN_FSS("open_fss", null, null, OpenFssHandler.class),

    EXIT_TO_HUD("display_hud", null, null, ExitToHud.class),
    CLOSE_SYSTEM_MAP("close_local_starsystem_map", null, null, ExitToHud.class),
    SET_HOME_SYSTEM("set_location_as_home_star_system", null, null, SetCurrentStarAsHomeSystem.class),
    TAKE_ME_HOME("take_us_home", null, null, PlotRouteToHomeHandler.class),
    PLOT_ROUTE_TO_CARRIER("plot_route_to_fleet_carrier", null, null, PlotRouteToMyFleetCarrier.class)
    ;


    private final String action;
    private final String placeholder;
    private final String paramKey;
    private final Class<? extends CommandHandler> handlerClass;

    CustomCommands(String action, String placeholder, String paramKey, Class<? extends CommandHandler> handlerClass) {
        this.action = action;
        this.placeholder = placeholder;
        this.paramKey = paramKey;
        this.handlerClass = handlerClass;
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

    public Class<? extends CommandHandler> getHandlerClass() {
        return handlerClass;
    }

    public String getCommandWithPlaceholder() {
        return placeholder != null ? action + " " + placeholder : action;
    }
}
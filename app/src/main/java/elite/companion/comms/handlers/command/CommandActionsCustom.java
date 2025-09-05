package elite.companion.comms.handlers.command;

import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

public enum CommandActionsCustom {
    SET_MINING_TARGET("set_mining_target", "<material>", "mining_target", SetMiningTargetHandler.class),
    PLOT_ROUTE("plot_route", "<destination>", "destination", SetRouteHandler.class),
    SET_PRIVACY_MODE("set_privacy_mode", "<privacy_mode_on_off>", "privacy_mode", SetPrivacyModeHandler.class),
    SET_RADIO_TRANSMISSION_MODDE("turn_radio_transmission_on_off", "<radio_transmission_on_off>", SystemSession.RADION_TRANSMISSION_ON_OFF, SetRadioTransmissionOnOff.class),
    CLEAR_SESSION("clear_session", "<session_clear>", "session_clear", ClearSessionHandler.class),

    SET_AI_VOICE("set_or_change_voice_to", "<voice_name>", "voice_name", SetAiVoice.class),
    SET_PROFILE("set_profile", "<profile>", PlayerSession.PROFILE, SetCadenceHandler.class),
    SET_PERSONALITY("set_personality", "<personality>", PlayerSession.PERSONALITY, SetPersonalityHandler.class),


    INCREASE_ENGINES_POWER("transfer_power_to_engines", "<power>", "power_to_engines", SetPowerToEnginesHandler.class),
    INCREASE_SYSTEMS_POWER("transfer_power_to_systems", "<power>", "power_to_systems", SetPowerToSystemsHandler.class),
    INCREASE_SHIELDS_POWER("transfer_power_to_shields", "<power>", "power_to_shields", SetPowerToSystemsHandler.class),//same as systems, handles different phrasing
    INCREASE_WEAPONS_POWER("transfer_power_to_weapons", "<power>", "power_to_weapons", SetPowerToWeaponsHandler.class),

    FSSDISCOVERY_SCAN("exploration_fssdiscovery_scan", "ExplorationFSSDiscoveryScan", "<custom>", PerformFSSScanHandler.class),
    SET_OPTIMAL_SPEED("set_optimal_speed", null, null, SetOptimalSpeedHandler.class),
    OPEN_GALAXY_MAP("open_galaxy_map", null, null, OpenGalaxyMapHandler.class),
    CLOSE_GALAXY_MAP("close_galaxy_map", null, null, CloseMapHandler.class),

    OPEN_SYSTEM_MAP("open_local_starsystem_map", null, null, OpenSystemMapHandler.class),
    CLOSE_SYSTEM_MAP("close_local_starsystem_map", null, null, CloseMapHandler.class),
    ;


    private final String action;
    private final String placeholder;
    private final String paramKey;
    private final Class<? extends CommandHandler> handlerClass;

    CommandActionsCustom(String action, String placeholder, String paramKey, Class<? extends CommandHandler> handlerClass) {
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
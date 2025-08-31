package elite.companion.comms.handlers.command;

import elite.companion.session.SystemSession;

public enum CommandActionsCustom {
    SET_MINING_TARGET("set_mining_target", "<material>", "mining_target", SetMiningTargetHandler.class),
    PLOT_ROUTE("plot_route", "<destination>", "destination", SetRouteHandler.class),
    SET_PRIVACY_MODE("set_privacy_mode", "<privacy_mode_on_off>", SystemSession.PRIVACY_MODE, SetPrivacyMode.class),
    SET_RADIO_TRANSMISSION_MODDE("turn_radio_transmission_on_off", "<radio_transmission_on_off>", SystemSession.RADION_TRANSMISSION_ON_OFF, SetRadioTransmissionOnOff.class),
    SET_AI_VOICE("set_or_change_voice_to", "<voice_name>", "voice_name", SetAiVoice.class),
    ANNOUNCE_STELLAR_BODY_SCANS("announce_stellar_body_scans", "<on_off>", SystemSession.ANNOUNCE_BODY_SCANS, SetAnnounceBodyScansHandler.class),
    SET_PROFILE("set_profile", "<profile>", SystemSession.PROFILE, SetCadenceHandler.class),
    SET_PERSONALITY("set_personality", "<personality>", SystemSession.PERSONALITY, SetPersonalityHandler.class),


    //INCREASE_ENGINES_POWER_BUGGY("increase_engines_power_buggy", "IncreaseEnginesPower_Buggy", "<power>", GenericGameController.class),
    INCREASE_ENGINES_POWER("full_to_engines", "IncreaseEnginesPower", "<power>", SetPowerToEnginesHandler.class),
    INCREASE_SYSTEMS_POWER("full_power_to_systems", "IncreaseSystemsPower", "<power>", SetPowerToSystemsHandler.class),
    INCREASE_SHIELDS_POWER("full_power_to_shields", "IncreaseSystemsPower", "<power>", SetPowerToSystemsHandler.class),
    INCREASE_WEAPONS_POWER("full_power_to_weapons", "IncreaseWeaponsPower", "<power>", SetPowerToWeaponsHandler.class),
    FSSDISCOVERY_SCAN("exploration_fssdiscovery_scan", "ExplorationFSSDiscoveryScan", "<custom>", PerformFSSScanHandler.class),
    SET_OPTIMAL_SPEED("set_optimal_speed", null, null, SetOptimalSpeedHandler.class)
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
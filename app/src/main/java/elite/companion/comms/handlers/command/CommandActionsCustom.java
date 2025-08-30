package elite.companion.comms.handlers.command;

import elite.companion.session.SystemSession;

/**
 * Defines custom actions for the VoiceController. such as queries, or system actions like automatic mining target setting.
 * The game controller commands are loaded from the bindings.
 *
 */
public enum CommandActionsCustom {


    SET_MINING_TARGET("set_mining_target", "<material>", "mining_target"),
    PLOT_ROUTE("plot_route", "<destination>", "destination"),
    SET_PRIVACY_MODE("set_privacy_mode", "<privacy_mode_on_off>", SystemSession.PRIVACY_MODE),
    SET_RADIO_TRANSMISSION_MODDE("turn_radio_transmission_on_off", "<radio_transmission_on_off>", SystemSession.RADION_TRANSMISSION_ON_OFF),
    SET_AI_VOICE("set_or_change_voice_to","voice_name", "voice_name"),
    ANNOUNCE_STELLAR_BODY_SCANS("announce_stellar_body_scans", "<on_off>", SystemSession.ANNOUNCE_BODY_SCANS),
    SET_CADENCE("set_cadence", "<cadence>", SystemSession.CADENCE),
    SET_PERSONALITY("set_personality", "<personality>", SystemSession.PERSONALITY),



    ; //<-- end of enum

    private final String action;
    private final String placeholder;
    private final String paramKey;

    CommandActionsCustom(String action, String placeholder, String paramKey) {
        this.action = action;
        this.placeholder = placeholder;
        this.paramKey = paramKey;
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

    public String getCommandWithPlaceholder() {
        return placeholder != null ? action + " " + placeholder : action;
    }
}
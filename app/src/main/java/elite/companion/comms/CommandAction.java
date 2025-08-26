package elite.companion.comms;

/**
 * Defines custom actions for the VoiceController. such as queries, or system actions like automatic mining target setting.
 * The game controller commands are loaded from the bindings.
 *
 */
public enum CommandAction {
    SET_MINING_TARGET("set_mining_target", "<material>", "mining_target"),
    PLOT_ROUTE("plot_route", "<destination>", "destination"),
    FIND_NEAREST_MATERIAL_TRADER("find_nearest_material_trader", null, null),
    GET_CURRENT_SYSTEM("get_current_system", null, null);

    private final String action;
    private final String placeholder;
    private final String paramKey;

    CommandAction(String action, String placeholder, String paramKey) {
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
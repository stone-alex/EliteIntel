package elite.companion.comms;

public enum CommandAction {
    SET_MINING_TARGET("set_mining_target", "<material>", "mining_target"),
    PLOT_ROUTE("plot_route", "<destination>", "destination"),
    FIND_NEAREST_MATERIAL_TRADER("find_nearest_material_trader", null, null),
    ACCESS_SHIP_DATA("access_ship_data", null, null),
    QUIRY_SHIP_DATA("query_ship_data", "<data_to_find>", "data to find");

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
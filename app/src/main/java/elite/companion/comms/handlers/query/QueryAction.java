package elite.companion.comms.handlers.query;

public enum QueryAction {
    //Queries
    QUERY_SEARCH_SIGNAL_DATA("query_search_local_signals_data", null, null),
    QUERY_SHIP_LOADOUT("query_ship_loadout", null, null),
    QUERY_ANALYZE_ROUTE("query_analyze_route", null, null),
    QUERY_FIND_NEAREST_MATERIAL_TRADER("query_find_nearest_material_trader", null, null),

    ; //<-- end of enum

    private final String action;
    private final String placeholder;
    private final String paramKey;

    QueryAction(String action, String placeholder, String paramKey) {
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

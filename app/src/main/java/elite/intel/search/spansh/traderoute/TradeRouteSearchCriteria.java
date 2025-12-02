package elite.intel.search.spansh.traderoute;

import elite.intel.search.spansh.client.StringQuery;

import java.util.StringJoiner;

public class TradeRouteSearchCriteria implements StringQuery {

    private static int TWELVE_FOUR_HOURS = 43200;

    private int maxHops;
    private int maxHopDistance;
    private String system;
    private String station;
    private int startingCapital;
    private int maxCargo;
    private int maxSystemDistance;
    private boolean requiresLargePad;
    private boolean allowProhibited;
    private boolean allowPlanetary;
    private boolean allowFleetCarriers;
    private boolean allowPermit;

    public void setMaxJumps(int maxHops) {
        this.maxHops = maxHops;
    }

    public void setMaxJumpDistance(int maxHopDistance) {
        this.maxHopDistance = maxHopDistance;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public void setStartingCapital(int startingCapital) {
        this.startingCapital = startingCapital;
    }

    public void setMaxCargo(int maxCargo) {
        this.maxCargo = maxCargo;
    }

    public void setMaxSystemDistance(int maxSystemDistance) {
        this.maxSystemDistance = maxSystemDistance;
    }

    public void setRequiresLargePad(boolean requiresLargePad) {
        this.requiresLargePad = requiresLargePad;
    }

    public void setAllowProhibited(boolean allowProhibited) {
        this.allowProhibited = allowProhibited;
    }

    public void setAllowPlanetary(boolean allowPlanetary) {
        this.allowPlanetary = allowPlanetary;
    }

    public void setAllowFleetCarriers(boolean allowFleetCarriers) {
        this.allowFleetCarriers = allowFleetCarriers;
    }


    public void setAllowPermit(boolean allowPermit) {
        this.allowPermit = allowPermit;
    }

    public int getMaxJumps() {
        return maxHops;
    }

    public int getMaxJumpDistance() {
        return maxHopDistance;
    }

    public String getSystem() {
        return system;
    }

    public String getStation() {
        return station;
    }

    public int getStartingCapital() {
        return startingCapital;
    }

    public int getMaxCargo() {
        return maxCargo;
    }

    public int getMaxSystemDistance() {
        return maxSystemDistance;
    }

    public boolean isRequiresLargePad() {
        return requiresLargePad;
    }

    public boolean isAllowProhibited() {
        return allowProhibited;
    }

    public boolean isAllowPlanetary() {
        return allowPlanetary;
    }

    public boolean isAllowFleetCarriers() {
        return allowFleetCarriers;
    }

    public boolean isAllowPermit() {
        return allowPermit;
    }

    @Override public String getQuery() {

        String criteria = "max_hops=" + maxHops +
                "&max_hop_distance=" + maxHopDistance
                + "&system=" + system
                + "&station=" + station
                + "&starting_capital=" + startingCapital
                + "&max_cargo=" + maxCargo
                + "&max_system_distance=" + maxSystemDistance
                + (requiresLargePad ? "&requires_large_pad=1" : "")
                + (allowProhibited ? "&allow_prohibited=1":"")
                + (allowPlanetary ? "&allow_planetary=1":"")
                + (allowFleetCarriers ? "&allow_player_owned=1":"")
//                + "&allow_restricted_access=0"
//                + "&unique=0"
                + "&max_price_age=" + TWELVE_FOUR_HOURS
                + (allowPermit ? "&permit=1":"");
        return criteria.replace(" ", "%20");
    }

    //max_hops=5&max_hop_distance=50&system=Tir&station=Anand+Metallurgic+Base&starting_capital=25000000&max_cargo=250&max_system_distance=10000000&requires_large_pad=1&allow_prohibited=0&allow_planetary=0&allow_player_owned=0&allow_restricted_access=0&unique=0&permit=0
    @Override public String toString() {
        return new StringJoiner(", ", "[", "]")
                .add("maxHops=" + maxHops)
                .add("maxHopDistance=" + maxHopDistance)
                .add("system='" + system + "'")
                .add("station='" + station + "'")
                .add("startingCapital=" + startingCapital)
                .add("maxCargo=" + maxCargo)
                .add("maxSystemDistance=" + maxSystemDistance)
                .add("requiresLargePad=" + requiresLargePad)
                .add("allowProhibited=" + allowProhibited)
                .add("allowPlanetary=" + allowPlanetary)
                .add("allowPlayerOwned=" + allowFleetCarriers)
                .add("allowPermit=" + allowPermit)
                .toString();
    }
}

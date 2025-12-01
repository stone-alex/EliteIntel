package elite.intel.search.spansh.traderoute;

import elite.intel.search.spansh.client.StringQuery;

import java.util.StringJoiner;

public class TradeRouteSearchCriteria implements StringQuery {

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

    public void setMaxDistanceFromStar(int maxHopDistance) {
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

    public int getMaxDistanceFromStar() {
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
                + "&requires_large_pad=" + (requiresLargePad ? 1 : 0)
                + "&allow_prohibited=" + (allowProhibited ? 1 : 0)
                + "&allow_planetary=" + (allowPlanetary ? 1 : 0)
                + "&allow_player_owned=" + (allowFleetCarriers ? 1 : 0)
                + "&allow_restricted_access=0"
                + "&unique=0"
                + "max_price_age=86400" //request data that is no older than 24 hours
                + "&permit=" + (allowPermit ? 1 : 0);
        return criteria.replace(" ", "%20");
    }

    @Override public String toString() {
        return new StringJoiner(", ",  "[", "]")
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

package elite.intel.search.spansh.traderoute;

import elite.intel.db.managers.LocationManager;
import elite.intel.gameapi.data.PowerPlayData;
import elite.intel.search.spansh.starsystems.StarSystemClient;
import elite.intel.search.spansh.starsystems.StarSystemResult;
import elite.intel.search.spansh.starsystems.SystemSearchCriteria;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.List;

public class TradeRouteFilter {

    private static TradeRouteFilter instance;

    private StarSystemClient client = StarSystemClient.getInstance();
    private PlayerSession playerSession = PlayerSession.getInstance();
    private LocationManager locationManager = LocationManager.getInstance();

    private TradeRouteFilter() {
    }

    public static synchronized TradeRouteFilter getInstance() {
        if (instance == null) {
            instance = new TradeRouteFilter();
        }
        return instance;
    }

    public TradeRouteResponse filter(TradeRouteResponse route) {

        if (route == null) return route; // no filter needed
        String pledgedPower = playerSession.getRankAndProgressDto().getPledgedToPower();
        if (pledgedPower == null) return route; // no filter needed

        List<TradeRouteTransaction> originalResults = route.getResult();
        List<TradeRouteTransaction> filteredResults = new ArrayList<>();

        for (TradeRouteTransaction transaction : originalResults) {
            String ogSource = transaction.getSource().getSystem();
            String ogDestination = transaction.getDestination().getSystem();

            StarSystemResult ogSourceSystem = searchSystem(ogSource);
            StarSystemResult ogDestinationSystem = searchSystem(ogDestination);

            // check if no results
            StarSystemResult.SystemRecord ogSourceResult = ogSourceSystem.getRecord();
            StarSystemResult.SystemRecord ogDestinationResult = ogDestinationSystem.getRecord();
            if (ogSourceResult == null || ogDestinationResult == null) continue;


            //toLowerCase inside method
            boolean isValidSourcePower = PowerPlayData.hasPower(ogSourceResult.getControllingPower());
            boolean isValidDestinationPower = PowerPlayData.hasPower(ogDestinationResult.getControllingPower());

            boolean isSourceStrongHold = isValidSourcePower && "stronghold".equalsIgnoreCase(ogSourceResult.getPowerState());
            boolean isDestinationStrongHold = isValidDestinationPower && "stronghold".equalsIgnoreCase(ogDestinationResult.getPowerState());

            // ADD NEUTRAL POWER
            if (!isValidSourcePower || !isValidDestinationPower) {
                filteredResults.add(transaction);
                continue;
            }


            boolean isFriendlySourcePower = isValidSourcePower ? ogSourceResult
                    .getControllingPower()
                    .toLowerCase()
                    .contains(pledgedPower.toLowerCase()) : false;

            boolean isFriendlyDestinationPower = isValidDestinationPower ? ogDestinationResult
                    .getControllingPower()
                    .toLowerCase()
                    .contains(pledgedPower.toLowerCase()) : false;

            // ADD FRIENDLY POWER
            if (isFriendlySourcePower && isFriendlyDestinationPower) {
                filteredResults.add(transaction);
            } else if (!isSourceStrongHold && !isDestinationStrongHold) {
                // ADD NON-FRIENDLY non stronghold POWER
                filteredResults.add(transaction);
            }

            route.setResult(filteredResults);
        }
        return route;
    }

    private StarSystemResult searchSystem(String starSystemName) {
        SystemSearchCriteria criteria = new SystemSearchCriteria();
        SystemSearchCriteria.Filters filters = new SystemSearchCriteria.Filters();
        SystemSearchCriteria.SystemNameFilter systemName = new SystemSearchCriteria.SystemNameFilter();
        systemName.setValue(starSystemName);
        filters.setSystemName(systemName);
        criteria.setFilters(filters);
        criteria.setPage(1);
        criteria.setSize(10);
        return client.search(criteria);
    }
}

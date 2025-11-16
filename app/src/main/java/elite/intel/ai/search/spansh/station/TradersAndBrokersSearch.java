package elite.intel.ai.search.spansh.station;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.station.traderandbroker.*;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.DestinationReminder;
import elite.intel.session.PlayerSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TradersAndBrokersSearch {

    private static TradersAndBrokersSearch instance;

    private TradersAndBrokersSearch() {
    }

    public static synchronized TradersAndBrokersSearch getInstance() {
        if (instance == null) {
            instance = new TradersAndBrokersSearch();
        }
        return instance;
    }


    public String location(TraderType traderType, BrokerType brokerType, Number maxDistance) {
        int distanceInLightYears = maxDistance == null ? 250 : maxDistance.intValue();
        PlayerSession playerSession = PlayerSession.getInstance();
        PlayerSession.GalacticCoordinates galacticCoordinates = playerSession.getGalacticCoordinates();

        TraderAndBrokerSearchCriteria criteria = new TraderAndBrokerSearchCriteria();
        criteria.setSize(1); // one page
        criteria.setPage(5); // 5 results per page

        TraderAndBrokerSearchCriteria.ReferenceCoords coordinates = new TraderAndBrokerSearchCriteria.ReferenceCoords();

        coordinates.setX(galacticCoordinates.x());
        coordinates.setY(galacticCoordinates.y());
        coordinates.setZ(galacticCoordinates.z());

        criteria.setReferenceCoords(coordinates);
        criteria.setSort(new ArrayList<>());

        TraderAndBrokerSearchCriteria.Filters filters = new TraderAndBrokerSearchCriteria.Filters();
        TraderAndBrokerSearchCriteria.Distance distance = new TraderAndBrokerSearchCriteria.Distance();
        distance.setMin(0);
        distance.setMax(distanceInLightYears);
        filters.setDistance(distance);

        if (traderType != null) {
            TraderAndBrokerSearchCriteria.MaterialTrader trader = new TraderAndBrokerSearchCriteria.MaterialTrader();
            trader.setValue(
                    Arrays.asList(
                            traderType.getType()
                    )
            );
            filters.setMaterialTrader(trader);
        } else if (brokerType != null) {
            TraderAndBrokerSearchCriteria.TechnologyBroker broker = new TraderAndBrokerSearchCriteria.TechnologyBroker();
            broker.setValue(
                    Arrays.asList(
                            brokerType.getType()
                    )
            );
            filters.setTechnologyBroker(broker);
        }

        criteria.setFilters(filters);
        List<TraderAndBrokerSearchDto.Result> results = SearchForMaterialBrokerOrTreder.findMaterialTrader(criteria);

        if (results == null || results.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No raw material traders found."));
            return null;
        }

        TraderAndBrokerSearchDto.Result result = results.get(0);
        DestinationReminder.getInstance().setDestination(result);
        return result.getSystemName();
    }
}

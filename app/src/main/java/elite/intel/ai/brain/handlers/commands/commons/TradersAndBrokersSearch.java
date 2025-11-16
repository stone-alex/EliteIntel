package elite.intel.ai.brain.handlers.commands.commons;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.search.spansh.traderandbroker.*;
import elite.intel.gameapi.EventBusManager;
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


    public String location(TraderType traderType, BrokerType brokerType, int maxDistance) {

        PlayerSession playerSession = PlayerSession.getInstance();
        PlayerSession.GalacticCoordinates galacticCoordinates = playerSession.getGalacticCoordinates();

        MaterialTraderOrBrokerSearchCriteria criteria = new MaterialTraderOrBrokerSearchCriteria();
        criteria.setSize(1);
        criteria.setPage(5);

        MaterialTraderOrBrokerSearchCriteria.ReferenceCoords coordinates = new MaterialTraderOrBrokerSearchCriteria.ReferenceCoords();

        coordinates.setX(galacticCoordinates.x());
        coordinates.setY(galacticCoordinates.y());
        coordinates.setZ(galacticCoordinates.z());

        criteria.setReferenceCoords(coordinates);
        criteria.setSort(new ArrayList<>());

        MaterialTraderOrBrokerSearchCriteria.Filters filters = new MaterialTraderOrBrokerSearchCriteria.Filters();
        MaterialTraderOrBrokerSearchCriteria.Distance distance = new MaterialTraderOrBrokerSearchCriteria.Distance();
        distance.setMin(0);
        distance.setMax(maxDistance);
        filters.setDistance(distance);

        if (traderType != null) {
            MaterialTraderOrBrokerSearchCriteria.MaterialTrader trader = new MaterialTraderOrBrokerSearchCriteria.MaterialTrader();
            trader.setValue(
                    Arrays.asList(
                            traderType.getType()
                    )
            );
            filters.setMaterialTrader(trader);
        } else if (brokerType != null) {
            MaterialTraderOrBrokerSearchCriteria.TechnologyBroker broker = new MaterialTraderOrBrokerSearchCriteria.TechnologyBroker();
            broker.setValue(
                    Arrays.asList(
                            brokerType.getType()
                    )
            );
            filters.setTechnologyBroker(broker);
        }

        criteria.setFilters(filters);
        List<TraderOrBrokerSearchDto.Result> results = SearchForMaterialBrokerOrTreder.findMaterialTrader(criteria);

        if (results == null || results.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("No raw material traders found."));
            return null;
        }

        return results.get(0).getSystemName();
    }
}

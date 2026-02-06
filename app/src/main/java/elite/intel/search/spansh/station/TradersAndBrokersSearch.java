package elite.intel.search.spansh.station;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.db.dao.LocationDao;
import elite.intel.db.managers.LocationManager;
import elite.intel.search.spansh.station.traderandbroker.*;
import elite.intel.db.managers.ReminderManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
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
        LocationManager locationManager = LocationManager.getInstance();
        LocationDao.Coordinates galacticCoordinates = locationManager.getGalacticCoordinates();

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
                    Collections.singletonList(
                            traderType.getType()
                    )
            );
            filters.setMaterialTrader(trader);
        } else if (brokerType != null) {
            TraderAndBrokerSearchCriteria.TechnologyBroker broker = new TraderAndBrokerSearchCriteria.TechnologyBroker();
            broker.setValue(
                    Collections.singletonList(
                            brokerType.getType()
                    )
            );
            filters.setTechnologyBroker(broker);
        }

        criteria.setFilters(filters);
        List<TraderAndBrokerSearchDto.Result> results = SearchForMaterialBrokerOrTrader.findMaterialTrader(criteria);

        if (results == null || results.isEmpty()) {
            EventBusManager.publish(new AiVoxResponseEvent("Suitable match found."));
            return null;
        }

        TraderAndBrokerSearchDto.Result result = results.get(0);
        EventBusManager.publish(
                new AiVoxResponseEvent("Head to " + result.getSystemName() + " star system. When you get there looks for" + result.getStationName()
                        +". Data was last updated: "+ TimeUtils.transformToYMDHtimeAgo(result.getUpdatedAt(), TimeUtils.LOCAL_DATE_TIME)
                )
        );
        ReminderManager.getInstance().setDestination(result.toJson());
        return result.getSystemName();
    }
}

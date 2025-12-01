package elite.intel.db.managers;

import elite.intel.db.dao.TradeRouteDao;
import elite.intel.db.util.Database;
import elite.intel.search.spansh.traderoute.TradeRouteLeg;
import elite.intel.search.spansh.traderoute.TradeRouteClient;
import elite.intel.search.spansh.traderoute.TradeRouteResponse;
import elite.intel.search.spansh.traderoute.TradeRouteStationInfo;

import java.util.List;

public class TradeRouteManager {

    private static TradeRouteManager instance;
    private TradeRouteManager() {
    }

    public static synchronized TradeRouteManager getInstance() {
        if (instance == null) {
            instance = new TradeRouteManager();
        }
        return instance;
    }

    public TradeRouteResponse calculateTradeRoute() {
        TradeProfileManager profileManager = TradeProfileManager.getInstance();
        TradeRouteClient client = TradeRouteClient.getInstance();
        TradeRouteResponse tradeRoute = client.getTradeRoute(profileManager.getCriteria());
        save(tradeRoute);
        return tradeRoute;
    }


    private void save(TradeRouteResponse tradeRoute) {
        Database.withDao(TradeRouteDao.class, dao -> {
            List<TradeRouteLeg> result = tradeRoute.getResult();
            long counter = 0;
            for(TradeRouteLeg stop : result) {
                LegSourceTuple<Long, TradeRouteDao.TradeRoute> entity = toTradeRouteLeg(
                        stop, new TradeRouteDao.TradeRoute(), counter, stop.getSource()
                );
                dao.save(entity.getEntity());
                counter = entity.getLegNumber();

                LegSourceTuple<Long, TradeRouteDao.TradeRoute> destinationEntity = toTradeRouteLeg(
                        stop, new TradeRouteDao.TradeRoute(), counter, stop.getDestination()
                );
                dao.save(destinationEntity.getEntity());
            }
            return Void.class;
        });
    }

    private LegSourceTuple toTradeRouteLeg(TradeRouteLeg stop, TradeRouteDao.TradeRoute legSource, long counter, TradeRouteStationInfo source) {
        legSource.setDistanceToArrival(stop.getDistance());
        legSource.setJson(stop.toJson());
        legSource.setLegNumber(counter++);
        legSource.setMarketId(source.getMarketId());
        legSource.setStarSystem(source.getSystem());
        legSource.setStation(source.getStation());
        ++counter;
        return new LegSourceTuple<>(counter, legSource);
    }


    class LegSourceTuple<K, V> {
        private final K legNumber;
        private final V entity;

        public LegSourceTuple(K legNumber, V entity) {
            this.legNumber = legNumber;
            this.entity = entity;
        }

        public K getLegNumber() {
            return legNumber;
        }

        public V getEntity() {
            return entity;
        }
    }
}

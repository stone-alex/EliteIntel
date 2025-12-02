package elite.intel.db.managers;

import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.TradeRouteDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.traderoute.*;
import elite.intel.util.json.GsonFactory;

import java.util.List;
import java.util.Optional;

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


    public TradeRouteLegTuple<TradeCommodityInfo, TradeRouteStationInfo> getNextStop() {
        return Database.withDao(
                TradeRouteDao.class, dao -> {
                    TradeRouteDao.TradeRoute stop = dao.getNextStop();
                    return new TradeRouteLegTuple<>(
                            GsonFactory.getGson().fromJson(stop.getCommodityInfoJson(), TradeCommodityInfo.class),
                            GsonFactory.getGson().fromJson(stop.getStationInfoJson(), TradeRouteStationInfo.class)
                    );
                }
        );
    }

    public TradeRouteLegTuple<TradeCommodityInfo, TradeRouteStationInfo> findForStarSystem(String starSystem) {
        return Database.withDao(
                TradeRouteDao.class, dao -> {
                    TradeRouteDao.TradeRoute stop = dao.findForStarSystem(starSystem);
                    return new TradeRouteLegTuple<>(
                            GsonFactory.getGson().fromJson(stop.getCommodityInfoJson(), TradeCommodityInfo.class),
                            GsonFactory.getGson().fromJson(stop.getStationInfoJson(), TradeRouteStationInfo.class)
                    );
                }
        );
    }


    public void deleteForStarSystem(String starSystem) {
        Database.withDao(TradeRouteDao.class, dao -> {
            dao.deleteForStarSystem(starSystem);
            return Void.class;
        });
    }

    public void clear() {
        Database.withDao(TradeRouteDao.class, dao -> {
            dao.clear();
            return null;
        });
    }


    public TradeRouteResponse calculateTradeRoute(TradeRouteSearchCriteria criteria) {
        TradeRouteClient client = TradeRouteClient.getInstance();
        TradeRouteResponse tradeRoute = client.getTradeRoute(criteria);
        if (tradeRoute == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Unable to calculate trade route."));
        } else {
            save(tradeRoute);
        }
        return tradeRoute;
    }


    private void save(TradeRouteResponse tradeRoute) {
        if (tradeRoute.getResult() == null) return;
        Database.withDao(TradeRouteDao.class, dao -> {
            dao.clear(); //clear previous route
            List<TradeRouteTransaction> result = tradeRoute.getResult();
            long counter = 0;
            for (TradeRouteTransaction transaction : result) {
                Optional<TradeCommodity> commodity = transaction.getCommodities().stream().findFirst();
                TradeCommodityInfo sourceCommodity = commodity.get().getSourceCommodity();
                TradeCommodityInfo destinationCommodity = commodity.get().getDestinationCommodity();

                /// save source stop
                TradeRouteTuple<Long, TradeRouteDao.TradeRoute> entity = toTradeRouteLeg(
                        sourceCommodity.toJson(),
                        transaction.getSource().toJson(),
                        transaction.getSource().getSystem(),
                        transaction.getSource().getStation(),
                        transaction.getCommodities().stream().findFirst().get().getName(),
                        counter
                );
                dao.save(entity.getEntity());

                /// save destination stop
                counter = entity.getLegNumber();
                TradeRouteTuple<Long, TradeRouteDao.TradeRoute> destinationEntity = toTradeRouteLeg(
                        destinationCommodity.toJson(),
                        transaction.getDestination().toJson(),
                        transaction.getDestination().getSystem(),
                        transaction.getDestination().getStation(),
                        transaction.getCommodities().stream().findFirst().get().getName(),
                        counter
                );
                dao.save(destinationEntity.getEntity());
            }
            return Void.class;
        });
    }

    private TradeRouteTuple toTradeRouteLeg(String commodityInfoJson, String tradeRouteStationInfoJson, String starSystem, String portName, String commodity, long counter) {
        TradeRouteDao.TradeRoute legSource = new TradeRouteDao.TradeRoute();
        legSource.setLegNumber(++counter);
        legSource.setCommodityInfoJson(commodityInfoJson);
        legSource.setStationInfoJson(tradeRouteStationInfoJson);
        legSource.setStarSystem(starSystem);
        legSource.setPortName(portName);
        legSource.setCommodityName(commodity);
        return new TradeRouteTuple<>(counter, legSource);
    }

    public boolean hasRoute() {
        return Database.withDao(TradeRouteDao.class, dao -> dao.getNextStop() != null);
    }


    class TradeRouteTuple<K, V> {
        private final K legNumber;
        private final V entity;

        public TradeRouteTuple(K legNumber, V entity) {
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

    public class TradeRouteLegTuple<K, V> {
        private final K commodityInfo;
        private final V tradeRouteStationInfo;

        public TradeRouteLegTuple(K commodityInfo, V tradeRouteStationInfo) {
            this.commodityInfo = commodityInfo;
            this.tradeRouteStationInfo = tradeRouteStationInfo;
        }

        public K getCommodityInfo() {
            return commodityInfo;
        }

        public V getTradeRouteStationInfo() {
            return tradeRouteStationInfo;
        }
    }
}

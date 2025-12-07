package elite.intel.db.managers;

import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.db.dao.TradeRouteDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.EventBusManager;
import elite.intel.search.spansh.station.marketstation.TradeStopDto;
import elite.intel.search.spansh.traderoute.*;
import elite.intel.util.json.GsonFactory;

import java.util.ArrayList;
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

    public List<TradeRouteLegTuple<Integer, TradeStopDto>> getAllStops() {
        return Database.withDao(TradeRouteDao.class, dao->{
            ArrayList<TradeRouteLegTuple<Integer, TradeStopDto>> result = new ArrayList<>();
            List<TradeRouteDao.TradeRoute> stops = dao.listAll();
            for(TradeRouteDao.TradeRoute stop : stops) {
                result.add(new TradeRouteLegTuple<>(stop.getLegNumber(), GsonFactory.getGson().fromJson(stop.getJson(), TradeStopDto.class)));
            }
            return result;
        });
    }

    public TradeRouteLegTuple<Integer, TradeStopDto> getNextStop() {
        return Database.withDao(
                TradeRouteDao.class, dao -> {
                    TradeRouteDao.TradeRoute stop = dao.getNextStop();
                    if(stop == null) return null;
                    return new TradeRouteLegTuple<>(
                            stop.getLegNumber(),
                            GsonFactory.getGson().fromJson(stop.getJson(), TradeStopDto.class)
                    );
                }
        );
    }

    public TradeRouteLegTuple<Integer, TradeStopDto> findForStarSystem(String starSystem) {
        return Database.withDao(
                TradeRouteDao.class, dao -> {
                    TradeRouteDao.TradeRoute stop = dao.findForStarSystem(starSystem);
                    return new TradeRouteLegTuple<>(
                            stop.getLegNumber(),
                            GsonFactory.getGson().fromJson(stop.getJson(), TradeStopDto.class)
                    );
                }
        );
    }


    public void deleteForMarketId(long starSystem) {
        Database.withDao(TradeRouteDao.class, dao -> {
            dao.deleteForMarketId(starSystem);
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
        if(tradeRoute == null){
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Unable to calculate trade route."));
            return null;
        }
        TradeRouteResponse filteredRoute = TradeRouteFilter.getInstance().filter(tradeRoute);

        if (filteredRoute == null) {
            EventBusManager.publish(new MissionCriticalAnnouncementEvent("Unable to calculate trade route."));
            return null;
        } else {
            save(filteredRoute);
        }
        return filteredRoute;
    }


    private void save(TradeRouteResponse tradeRoute) {
        if (tradeRoute.getResult() == null) return;
        Database.withDao(TradeRouteDao.class, dao -> {
            dao.clear(); //clear previous route

            List<TradeRouteTransaction> result = tradeRoute.getResult();
            int counter = 0;

            //transaction contains from and to address + list of commodities
            for (TradeRouteTransaction transaction : result) {
                List<TradeCommodity> commodities = transaction.getCommodities();
                TradeStopDto stop = new TradeStopDto(
                        ++counter,
                        commodities,
                        transaction.getSource().getSystem(),
                        transaction.getSource().getStation(),
                        transaction.getDestination().getSystem(),
                        transaction.getDestination().getStation(),
                        transaction.getSource().getMarketId(),
                        transaction.getDestination().getMarketId()

                );
                TradeRouteDao.TradeRoute data = new TradeRouteDao.TradeRoute();
                data.setJson(stop.toJson());
                data.setLegNumber(stop.getStopNumber());
                dao.save(data);
            }
            return Void.class;
        });
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
        private final K legNumber;
        private final V tradeStopDto;

        public TradeRouteLegTuple(K legNumber, V tradeStopDto) {
            this.legNumber = legNumber;
            this.tradeStopDto = tradeStopDto;
        }

        public K getLegNumber() {
            return legNumber;
        }

        public V getTradeStopDto() {
            return tradeStopDto;
        }
    }
}

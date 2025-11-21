package elite.intel.db;

import elite.intel.ai.search.spansh.market.StationMarketDto;
import elite.intel.db.dao.StationMarketDao;
import elite.intel.db.util.Database;
import elite.intel.util.json.GsonFactory;

import java.util.List;

public class StationMarkets {
    private static final StationMarkets INSTANCE = new StationMarkets();

    private StationMarkets() {
    }

    public static StationMarkets getInstance() {
        return INSTANCE;
    }

    public List<StationMarketDto> findForStation(String stationName) {
        return Database.withDao(StationMarketDao.class, dao -> {
            List<StationMarketDto> result = new java.util.ArrayList<>();
            List<StationMarketDao.StationMarket> data = dao.findForStation(stationName);
            for (StationMarketDao.StationMarket entity : data) {
                result.add(GsonFactory.getGson().fromJson(entity.getJson(), StationMarketDto.class));
            }
            return result;
        });
    }

    public void addList(List<StationMarketDto> data) {
        Database.withDao(StationMarketDao.class, dao -> {
            for (StationMarketDto market : data) {
                StationMarketDao.StationMarket stationMarket = new StationMarketDao.StationMarket();
                stationMarket.setJson(market.toJson());
                stationMarket.setStationName(market.stationName());
                stationMarket.setMarketId(market.getMarketId());
                dao.upsert(stationMarket);
            }
            return null;
        });
    }

    public void clear() {
        Database.withDao(StationMarketDao.class, dao -> {
            dao.clear();
            return null;
        });
    }

    public List<StationMarketDto> listAll() {
        return Database.withDao(StationMarketDao.class, dao -> {
            StationMarketDao.StationMarket[] all = dao.listAll();
            List<StationMarketDto> result = new java.util.ArrayList<>();
            for (StationMarketDao.StationMarket entity : all) {
                result.add(GsonFactory.getGson().fromJson(entity.getJson(), StationMarketDto.class));
            }
            return result;
        });
    }
}

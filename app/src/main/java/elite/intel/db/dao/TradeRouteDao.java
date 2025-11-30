package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(TradeRouteDao.TradeRouteMapper.class)
public interface TradeRouteDao {


    @SqlUpdate("""
            INSERT OR REPLACE INTO trade_route (legNumber, marketId, station, starSystem, distanceToArrival, json) 
                VALUES (:legNumber, :marketId, :station, :starSystem, :distanceToArrival, :json)
                        on conflict do update set
                        marketId = excluded.marketId,
                        station = excluded.station,
                        starSystem= excluded.starSystem,
                        distanceToArrival = excluded.distanceToArrival,
                        json = excluded.json
            """)
    void save(@BindBean TradeRouteDao.TradeRoute data);

    @SqlQuery("SELECT * FROM trade_route WHERE legNumber = :legNumber")
    TradeRoute findTradeRouteLeg(long legNumber);


    class TradeRouteMapper implements RowMapper<TradeRoute> {

        @Override public TradeRoute map(ResultSet rs, StatementContext ctx) throws SQLException {
            TradeRoute route = new TradeRoute();
            route.setJson(rs.getString("json"));
            route.setMarketId(rs.getLong("marketId"));
            route.setStation(rs.getString("station"));
            route.setStarSystem(rs.getString("starSystem"));
            route.setDistanceToArrival(rs.getDouble("distanceToArrival"));
            route.setLegNumber(rs.getLong("legNumber"));
            route.setStarSystem(rs.getString("starSystem"));
            return route;
        }
    }


    class TradeRoute {

        private String json;
        private Long marketId;
        private String station;
        private String starSystem;
        private Double distanceToArrival;
        private Long legNumber;


        public TradeRoute() {
        }

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }

        public Long getMarketId() {
            return marketId;
        }

        public void setMarketId(Long marketId) {
            this.marketId = marketId;
        }

        public String getStation() {
            return station;
        }

        public void setStation(String station) {
            this.station = station;
        }

        public String getStarSystem() {
            return starSystem;
        }

        public void setStarSystem(String starSystem) {
            this.starSystem = starSystem;
        }

        public double getDistanceToArrival() {
            return distanceToArrival;
        }

        public void setDistanceToArrival(Double distanceToArrival) {
            this.distanceToArrival = distanceToArrival;
        }

        public Long getLegNumber() {
            return legNumber;
        }

        public void setLegNumber(Long legNumber) {
            this.legNumber = legNumber;
        }
    }
}

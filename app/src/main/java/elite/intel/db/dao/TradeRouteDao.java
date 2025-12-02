package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(TradeRouteDao.TradeRouteMapper.class)
public interface TradeRouteDao {

    @SqlUpdate("""
            INSERT OR REPLACE INTO trade_route (legNumber, commodityInfoJson, stationInfoJson, starSystem, portName, commodityName) 
                VALUES (:legNumber, :commodityInfoJson, :stationInfoJson, :starSystem, :portName, :commodityName)
                        on conflict do update set
                        stationInfoJson = excluded.stationInfoJson,
                        commodityInfoJson = excluded.commodityInfoJson,
                        starSystem = excluded.starSystem,
                        commodityName = excluded.commodityName
            """)
    void save(@BindBean TradeRouteDao.TradeRoute data);

    @SqlQuery("SELECT * FROM trade_route ORDER BY legNumber ASC LIMIT 1")
    TradeRoute getNextStop();

    @SqlUpdate("DELETE FROM trade_route")
    void clear();

    @SqlUpdate("DELETE FROM trade_route where starSystem= :starSystem")
    void deleteForStarSystem(@Bind String starSystem);

    @SqlQuery("SELECT * FROM trade_route where starSystem= :starSystem")
    TradeRoute findForStarSystem(String starSystem);


    class TradeRouteMapper implements RowMapper<TradeRoute> {

        @Override public TradeRoute map(ResultSet rs, StatementContext ctx) throws SQLException {
            TradeRoute route = new TradeRoute();
            route.setLegNumber(rs.getLong("legNumber"));
            route.setCommodityInfoJson(rs.getString("commodityInfoJson"));
            route.setStationInfoJson(rs.getString("stationInfoJson"));
            route.setStarSystem(rs.getString("starSystem"));
            route.setPortName(rs.getString("portName"));
            route.setCommodityName(rs.getString("commodityName"));
            return route;
        }
    }


    class TradeRoute {
        private String commodityInfoJson;
        private String stationInfoJson;
        private Long legNumber;
        private String starSystem;
        private String portName;
        private String commodityName;


        public TradeRoute() {
        }

        public String getCommodityInfoJson() {
            return commodityInfoJson;
        }

        public void setCommodityInfoJson(String commodityInfoJson) {
            this.commodityInfoJson = commodityInfoJson;
        }

        public Long getLegNumber() {
            return legNumber;
        }

        public void setLegNumber(Long legNumber) {
            this.legNumber = legNumber;
        }

        public String getStationInfoJson() {
            return stationInfoJson;
        }

        public void setStationInfoJson(String stationInfoJson) {
            this.stationInfoJson = stationInfoJson;
        }

        public String getStarSystem() {
            return starSystem;
        }

        public void setStarSystem(String starSystem) {
            this.starSystem = starSystem;
        }

        public String getPortName() {
            return portName;
        }

        public void setPortName(String portName) {
            this.portName = portName;
        }

        public void setCommodityName(String commodity) {
            this.commodityName = commodity;
        }

        public String getCommodityName() {
            return commodityName;
        }
    }
}

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
            INSERT OR REPLACE INTO trade_route (legNumber, json) 
                VALUES (:legNumber, :json)
                        on conflict do update set
                        json = excluded.json
            """)
    void save(@BindBean TradeRouteDao.TradeRoute data);

    @SqlQuery("SELECT * FROM trade_route ORDER BY legNumber ASC LIMIT 1")
    TradeRoute getNextStop();

    @SqlUpdate("DELETE FROM trade_route")
    void clear();

    @SqlUpdate("""
        DELETE FROM trade_route 
        WHERE CAST(json_extract(json, '$.destinationMarketId') AS INTEGER) = :marketId
    """)
    void deleteForMarketId(@Bind("marketId") long marketId);

    @SqlQuery("SELECT * FROM trade_route where json LIKE :pattern")
    TradeRoute findForStarSystem(@Bind("pattern") String pattern);


    class TradeRouteMapper implements RowMapper<TradeRoute> {

        @Override public TradeRoute map(ResultSet rs, StatementContext ctx) throws SQLException {
            TradeRoute route = new TradeRoute();
            route.setLegNumber(rs.getInt("legNumber"));
            route.setJson(rs.getString("json"));
            return route;
        }
    }


    class TradeRoute {
        private Integer legNumber;
        private String json;

        public TradeRoute() {
        }

        public Integer getLegNumber() {
            return legNumber;
        }

        public void setLegNumber(Integer legNumber) {
            this.legNumber = legNumber;
        }

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }
}

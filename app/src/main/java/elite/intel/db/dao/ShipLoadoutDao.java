package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(ShipLoadoutDao.ShipLoadoutMapper.class)
public interface ShipLoadoutDao {


    @SqlUpdate("INSERT OR REPLACE INTO ship_loadout (id, json) VALUES (1, :json)")
    void save(@BindBean ShipLoadoutDao.ShipLoadout data);

    @SqlQuery("SELECT json FROM ship_loadout WHERE id = 1")
    ShipLoadoutDao.ShipLoadout get();

    @SqlUpdate("DELETE FROM ship_loadout")
    void clear();


    class ShipLoadoutMapper implements RowMapper<ShipLoadoutDao.ShipLoadout> {

        @Override public ShipLoadout map(ResultSet rs, StatementContext ctx) throws SQLException {
            ShipLoadout shipLoadout = new ShipLoadout();
            shipLoadout.json = rs.getString("json");
            return shipLoadout;
        }
    }


    class ShipLoadout {
        private String json;

        public ShipLoadout() {
        }

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }
}

package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(FleetCarrierDao.FleetCarrierMapper.class)
public interface FleetCarrierDao {

    @SqlUpdate("INSERT OR REPLACE INTO fleet_carrier (id, json) VALUES (1, :json)")
    void save(@BindBean FleetCarrier fleetCarrier);

    @SqlQuery("SELECT * FROM fleet_carrier WHERE id = 1")
    FleetCarrier get();


    class FleetCarrierMapper implements RowMapper<FleetCarrierDao.FleetCarrier> {
        @Override public FleetCarrier map(ResultSet rs, StatementContext ctx) throws SQLException {
            FleetCarrier fleetCarrier = new FleetCarrier();
            fleetCarrier.setJson(rs.getString("json"));
            return fleetCarrier;
        }
    }


    class FleetCarrier {
        private String json;

        public FleetCarrier() {
        }

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }
}

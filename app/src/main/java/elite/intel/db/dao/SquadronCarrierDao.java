package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(SquadronCarrierDao.SquadronCarrierMapper.class)
public interface SquadronCarrierDao {

    @SqlUpdate("INSERT OR REPLACE INTO squadron_carrier (id, json) VALUES (1, :json)")
    void save(@BindBean SquadronCarrier squadronCarrier);

    @SqlQuery("SELECT * FROM squadron_carrier WHERE id = 1")
    SquadronCarrier get();

    class SquadronCarrierMapper implements RowMapper<SquadronCarrier> {
        @Override
        public SquadronCarrier map(ResultSet rs, StatementContext ctx) throws SQLException {
            SquadronCarrier carrier = new SquadronCarrier();
            carrier.setJson(rs.getString("json"));
            return carrier;
        }
    }

    class SquadronCarrier {
        private String json;

        public SquadronCarrier() {
        }

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }
}

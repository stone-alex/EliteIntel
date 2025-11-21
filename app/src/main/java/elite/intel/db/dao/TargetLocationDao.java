package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(TargetLocationDao.TargetLocationMapper.class)
public interface TargetLocationDao {


    @SqlUpdate("INSERT OR REPLACE INTO target_location (id, json) VALUES (1, :json)")
    void save(@BindBean TargetLocationDao.TargetLocation data);

    @SqlQuery("SELECT json FROM target_location WHERE id = 1")
    TargetLocationDao.TargetLocation get();




    class TargetLocationMapper implements RowMapper<TargetLocation> {

        @Override public TargetLocation map(ResultSet rs, StatementContext ctx) throws SQLException {
            TargetLocation location = new TargetLocation();
            location.setJson(rs.getString("json"));
            return location;
        }
    }


    class TargetLocation {

        public TargetLocation() {
        }

        private String json;

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }
}

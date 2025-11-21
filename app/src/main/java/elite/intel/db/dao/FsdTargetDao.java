package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(FsdTargetDao.FsdTargetMapper.class)
public interface FsdTargetDao {


    @SqlUpdate("INSERT OR REPLACE INTO fsd_target (id, json) VALUES (1, :json)")
    void save(@BindBean FsdTargetDao.FsdTarget data);

    @SqlQuery("SELECT json FROM fsd_target WHERE id = 1")
    FsdTarget get();


    class FsdTargetMapper implements RowMapper<FsdTarget> {

        @Override public FsdTarget map(ResultSet rs, StatementContext ctx) throws SQLException {
            FsdTarget fsdTarget = new FsdTarget();
            fsdTarget.setJson(rs.getString("json"));
            return fsdTarget;
        }
    }


    class FsdTarget {

        public FsdTarget() {
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

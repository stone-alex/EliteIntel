package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(ReputationDao.ReputationMapper.class)
public interface ReputationDao {


    @SqlUpdate("INSERT OR REPLACE INTO reputation (id, json) VALUES (1,:json)")
    void save(@BindBean ReputationDao.Reputation data);

    @SqlQuery("SELECT json FROM reputation WHERE id = 1")
    Reputation get();


    @SqlUpdate("DELETE FROM reputation")
    void clear();



    class ReputationMapper implements RowMapper<Reputation> {

        @Override public Reputation map(ResultSet rs, StatementContext ctx) throws SQLException {
            Reputation reputation = new Reputation();
            reputation.setJson(rs.getString("json"));
            return reputation;
        }
    }




    class Reputation {

        public Reputation() {
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

package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(RankAndProgressDao.RankAndProgressMapper.class)
public interface RankAndProgressDao {

    @SqlQuery("SELECT * FROM ranks_and_progress WHERE id = 1")
    RankAndProgressDao.RankAndProgress get();

    @SqlUpdate("""
            INSERT OR REPLACE INTO ranks_and_progress
            (id, json)
            VALUES(1, :json)
            """)
    void save(@BindBean RankAndProgressDao.RankAndProgress data);


    class RankAndProgressMapper implements RowMapper<RankAndProgress> {

        @Override public RankAndProgress map(ResultSet rs, StatementContext ctx) throws SQLException {
            RankAndProgress rankAndProgress = new RankAndProgress();
            rankAndProgress.setJson(rs.getString("json"));
            return rankAndProgress;
        }
    }


    class RankAndProgress {
        private String json;

        public RankAndProgress() {
        }

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }

}

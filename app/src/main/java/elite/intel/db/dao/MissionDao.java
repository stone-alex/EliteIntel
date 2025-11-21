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

@RegisterRowMapper(MissionDao.MissionMapper.class)
public interface MissionDao {

    @SqlUpdate("""
            INSERT OR REPLACE INTO missions (key, mission)
            VALUES(:key, :mission)
            ON CONFLICT(key) DO UPDATE SET
            mission = excluded.mission
            """)
    void upsert(@BindBean Mission mission);


    @SqlQuery("SELECT * FROM missions WHERE key = :key")
    Mission get(@Bind("key") Long key);

    @SqlQuery("SELECT * FROM missions")
    Mission[] listAll();


    @SqlUpdate("DELETE FROM missions WHERE key = :missionId")
    void delete(Long missionId);


    class MissionMapper implements RowMapper<Mission> {

        @Override public Mission map(ResultSet rs, StatementContext ctx) throws SQLException {
            Mission mission = new Mission();
            mission.setKey(rs.getLong("key"));
            mission.setMission(rs.getString("mission"));
            return mission;
        }
    }


    class Mission {
        private Long key;
        private String mission;

        public Mission() {
        }

        public Long getKey() {
            return key;
        }

        public void setKey(Long key) {
            this.key = key;
        }

        public String getMission() {
            return mission;
        }

        public void setMission(String mission) {
            this.mission = mission;
        }
    }
}

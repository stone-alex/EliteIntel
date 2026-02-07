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
import java.util.List;

@RegisterRowMapper(PirateMissionProviderDao.MissionProviderMapper.class)
public interface PirateMissionProviderDao {

    @SqlUpdate("""
            INSERT OR IGNORE INTO mission_provider (starSystem, targetSystem, x, y, z, missionProviderFaction, targetFactionID)
            VALUES (:starSystem, :targetSystem, :x, :y, :z, :missionProviderFaction, :targetFactionID)
            """)
    void upsert(@BindBean PirateMissionProviderDao.MissionProvider entity);


    @SqlQuery("""
            select * from mission_provider where targetSystem = :targetSystem and starSystem != :starSystem
            """)
    List<PirateMissionProviderDao.MissionProvider> findMissionProviderForTargetStarSystem(@Bind("targetSystem") String targetSystem, @Bind("starSystem") String starSystem);

    @SqlQuery("""
            SELECT mp.*
            FROM mission_provider mp
                     INNER JOIN (
                SELECT targetSystem, COUNT(*) AS cnt
                FROM mission_provider
                GROUP BY targetSystem
                ORDER BY cnt DESC
                LIMIT 1
            ) top ON mp.targetSystem = top.targetSystem
            ORDER BY mp.targetSystem, mp.id;
            """)
    List<PirateMissionProviderDao.MissionProvider> findProvidersWithMostTargetSystems();

    @SqlQuery("""
            SELECT * FROM mission_provider
                WHERE starSystem = :starSystem
                    AND targetFactionID = :targetFactionID
                    AND missionProviderFaction IS NULL LIMIT 1
            """)
    MissionProvider findNullForTarget(@Bind("starSystem") String starSystem, @Bind("targetFactionID") int targetFactionID);


    @SqlUpdate("UPDATE mission_provider SET missionProviderFaction = :faction WHERE id = :id")
    void updateFaction(@Bind("id") int id, @Bind("faction") String faction);

    @SqlUpdate("delete from mission_provider where id > 0")
    void clear();


    class MissionProviderMapper implements RowMapper<MissionProvider> {

        @Override public MissionProvider map(ResultSet rs, StatementContext ctx) throws SQLException {
            MissionProvider entity = new MissionProvider();
            entity.setId(rs.getInt("id"));
            entity.setStarSystem(rs.getString("starSystem"));
            entity.setTargetSystem(rs.getString("targetSystem"));
            entity.setX(rs.getDouble("x"));
            entity.setY(rs.getDouble("y"));
            entity.setZ(rs.getDouble("z"));
            entity.setMissionProviderFaction(rs.getString("missionProviderFaction"));
            entity.setTargetFactionID(rs.getInt("targetFactionID"));
            return entity;
        }
    }


    class MissionProvider {
        private int id;
        private String starSystem;
        private String targetSystem;
        private double x, y, z;
        private String missionProviderFaction;
        private int targetFactionID;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getStarSystem() {
            return starSystem;
        }

        public void setStarSystem(String starSystem) {
            this.starSystem = starSystem;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }

        public String getMissionProviderFaction() {
            return missionProviderFaction;
        }

        public void setMissionProviderFaction(String missionProviderFaction) {
            this.missionProviderFaction = missionProviderFaction;
        }

        public int getTargetFactionID() {
            return targetFactionID;
        }

        public void setTargetFactionID(int targetFactionID) {
            this.targetFactionID = targetFactionID;
        }

        public String getTargetSystem() {
            return targetSystem;
        }

        public void setTargetSystem(String targetSystem) {
            this.targetSystem = targetSystem;
        }
    }
}

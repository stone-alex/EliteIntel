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

@RegisterRowMapper(PirateHuntingGroundsDao.HuntingGroundMapper.class)
public interface PirateHuntingGroundsDao {

    @SqlUpdate("""
            INSERT OR IGNORE INTO pirate_hunting_grounds (starSystem, x, y, z, targetFaction, hasResSite,  ignored)
                VALUES (:starSystem, :x, :y, :z, :targetFaction, :hasResSite, :ignored)
            """)
    void save(@BindBean HuntingGround data);

    @SqlUpdate("""
            UPDATE pirate_hunting_grounds SET targetFaction = :targetFaction
                WHERE starSystem = :starSystem
            """)
    void updateTargetFaction(@Bind("starSystem") String starSystem, @Bind("targetFaction") String targetFaction);

    @SqlQuery("""
            select * from pirate_hunting_grounds where ignored = false
            ORDER BY
                ((x - :x)*(x - :x) + (y - :y)*(y - :y) + (z - :z)*(z - :z))
            LIMIT :limit;
            """)
    HuntingGround findNearest(@Bind("x") double x, @Bind("y") double y, @Bind("z") double z, @Bind("limit") int limit);

    @SqlQuery("""
            select * from pirate_hunting_grounds where targetFaction is null and hasResSite = false and ignored = false
            ORDER BY
                ((x - :x)*(x - :x) + (y - :y)*(y - :y) + (z - :z)*(z - :z));
            """)
    HuntingGround findNearestRecon(@Bind("x") double x, @Bind("y") double y, @Bind("z") double z);

    @SqlQuery("""
            SELECT * FROM pirate_hunting_grounds
                WHERE starSystem = :starSystem
            """)
    HuntingGround findByStarSystem(@Bind("starSystem") String starSystem);


    @SqlUpdate("""
            update pirate_hunting_grounds set ignored = true
                WHERE starSystem = :starSystem
            """)
    void ignoreHuntingGround(@Bind("starSystem") String starSystem);

    @SqlQuery("select * from pirate_hunting_grounds where targetFaction=:targetFaction and starSystem = :starSystem limit 1")
    HuntingGround findByFactionName(@Bind("targetFaction") String targetFaction, @Bind("starSystem") String targetSystem);

    @SqlQuery("""
        select starSystem from pirate_hunting_grounds where targetFaction=:targetFaction limit 1
    """)
    String findByFactionName(@Bind("targetFaction") String targetFaction);

    @SqlUpdate("update pirate_hunting_grounds set hasResSite = true where starSystem = :starSystem ")
    void confirm(@Bind("starSystem") String primaryStarName);


    @SqlUpdate("DELETE FROM pirate_hunting_grounds")
    void clear();

    class HuntingGroundMapper implements RowMapper<HuntingGround> {

        @Override public HuntingGround map(ResultSet rs, StatementContext ctx) throws SQLException {
            HuntingGround entity = new HuntingGround();
            entity.setId(rs.getInt("id"));
            entity.setStarSystem(rs.getString("starSystem"));
            entity.setX(rs.getDouble("x"));
            entity.setY(rs.getDouble("y"));
            entity.setZ(rs.getDouble("z"));
            entity.setTargetFaction(rs.getString("targetFaction"));
            entity.setHasResSite(rs.getBoolean("hasResSite"));
            return entity;
        }
    }


    class HuntingGround {
        private int id;
        private String starSystem;
        private double x, y, z;
        private String targetFaction;
        private boolean hasResSite;
        private boolean ignored;


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

        public String getTargetFaction() {
            return targetFaction;
        }

        public void setTargetFaction(String targetFaction) {
            this.targetFaction = targetFaction;
        }

        public boolean isHasResSite() {
            return hasResSite;
        }

        public void setHasResSite(boolean hasResSite) {
            this.hasResSite = hasResSite;
        }

        public boolean isIgnored() {
            return ignored;
        }

        public void setIgnored(boolean ignored) {
            this.ignored = ignored;
        }
    }
}

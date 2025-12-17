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

@RegisterRowMapper(PirateFactionDao.PirateFactionMapper.class)
public interface PirateFactionDao {

    @SqlUpdate("""
            INSERT OR IGNORE INTO pirate_factions (starSystem, x, y, z, targetFaction, hasResSite) 
                VALUES (:starSystem, :x, :y, :z, :targetFaction, :hasResSite)  
            """)
    void save(@BindBean PirateFaction data);

    @SqlUpdate("""
            UPDATE pirate_factions SET targetFaction = :targetFaction 
                WHERE starSystem = :starSystem
            """)
    void updateTargetFaction(@Bind("starSystem") String starSystem, @Bind("targetFaction") String targetFaction);

    @SqlQuery("""
            select * from pirate_factions
            ORDER BY
                ((x - :x)*(x - :x) + (y - :y)*(y - :y) + (z - :z)*(z - :z)) ASC
            LIMIT :limit;
            """)
    PirateFactionDao.PirateFaction findNearest(@Bind("x") double x, @Bind("y") double y, @Bind("z") double z, @Bind("limit") int limit);

    @SqlQuery("""
            select * from pirate_factions where targetFaction is null
            ORDER BY
                ((x - :x)*(x - :x) + (y - :y)*(y - :y) + (z - :z)*(z - :z)) ASC
            LIMIT :limit;
            """)
    PirateFactionDao.PirateFaction findNearestRecon(@Bind("x") double x, @Bind("y") double y, @Bind("z") double z, @Bind("limit") int limit);

    @SqlQuery("""
            SELECT * FROM pirate_factions 
                WHERE starSystem = :starSystem
            """)
    PirateFactionDao.PirateFaction findByStarSystem(@Bind("starSystem") String starSystem);

    @SqlQuery("select * from pirate_factions where targetFaction=:targetFaction and starSystem = :starSystem limit 1")
    PirateFaction findByFactionName(@Bind("targetFaction") String targetFaction, @Bind("starSystem") String targetSystem);


    @SqlUpdate("update pirate_factions set hasResSite = true where starSystem = :starSystem ")
    void confirm(@Bind("starSystem") String primaryStarName);


    class PirateFactionMapper implements RowMapper<PirateFaction> {

        @Override public PirateFaction map(ResultSet rs, StatementContext ctx) throws SQLException {
            PirateFaction entity = new PirateFaction();
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


    class PirateFaction {
        private int id;
        private String starSystem;
        private double x, y, z;
        private String targetFaction;
        private boolean hasResSite;


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
    }
}

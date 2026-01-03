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

@RegisterRowMapper(BrainTreesDao.BrainTreeRowMapper.class)
public interface BrainTreesDao {


    @SqlUpdate("""
                INSERT OR REPLACE INTO brain_trees (starSystem, json, x, y, z) VALUES (:starSystem, :json, :x, :y, :z)
                        on conflict do update set
                            json = excluded.json,
                            x = excluded.x,
                            y = excluded.y,
                            x = excluded.z
            """)
    void save(@BindBean BrainTreeLocation data);

    @SqlQuery("SELECT * FROM brain_trees where starSystem = :starSystem")
    BrainTreeLocation findForStarSystem(@Bind String starSystem);


    @SqlQuery("""
            SELECT *, 
                   ( (x - :x)*(x - :x) + (y - :y)*(y - :y) + (z - :z)*(z - :z) ) AS dist_sq
                FROM brain_trees
                ORDER BY dist_sq ASC
            LIMIT 1
            """)
    BrainTreeLocation findNearest(@Bind("x") double x,
                                  @Bind("y") double y,
                                  @Bind("z") double z);

    @SqlQuery("SELECT COUNT(*) FROM brain_trees")
    int count();


    @SqlQuery("""
            select * from brain_trees where json like '%' || :material || '%'
            ORDER BY
                ((x - :x)*(x - :x) + (y - :y)*(y - :y) + (z - :z)*(z - :z)) ASC
            LIMIT :limit;
            """)
    List<BrainTreeLocation> findByMaterialNearest(
            @Bind("material") String material,
            @Bind("x") double x,
            @Bind("y") double y,
            @Bind("z") double z,
            @Bind("limit") int limit
    );

    class BrainTreeRowMapper implements RowMapper<BrainTreeLocation> {

        @Override public BrainTreeLocation map(ResultSet rs, StatementContext ctx) throws SQLException {
            BrainTreeLocation entity = new BrainTreeLocation();
            entity.setStarSystem(rs.getString("starSystem"));
            entity.setJson(rs.getString("json"));
            entity.setX(rs.getDouble("x"));
            entity.setY(rs.getDouble("y"));
            entity.setZ(rs.getDouble("z"));
            return entity;
        }
    }


    class BrainTreeLocation {
        private String starSystem;
        private String json;
        private Double x;
        private Double y;
        private Double z;

        public String getStarSystem() {
            return starSystem;
        }

        public void setStarSystem(String starSystem) {
            this.starSystem = starSystem;
        }

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }

        public Double getX() {
            return x;
        }

        public void setX(Double x) {
            this.x = x;
        }

        public Double getY() {
            return y;
        }

        public void setY(Double y) {
            this.y = y;
        }

        public Double getZ() {
            return z;
        }

        public void setZ(Double z) {
            this.z = z;
        }
    }
}

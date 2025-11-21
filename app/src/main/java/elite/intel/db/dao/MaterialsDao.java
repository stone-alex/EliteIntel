package elite.intel.db.dao;

import elite.intel.util.json.GsonFactory;
import elite.intel.util.json.ToJsonConvertible;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface MaterialsDao {

    @SqlUpdate("""
            INSERT INTO materials (materialName, materialType, amount, maxCapacity) 
            VALUES (:materialName, :materialType, :amount, :maxCapacity)
            ON CONFLICT(materialName) DO UPDATE SET
                amount = excluded.amount,
                maxCapacity = excluded.maxCapacity
            """)
    void upsert(@Bind("materialName") String materialName,
                @Bind("materialType") String materialType,
                @Bind("amount") int amount,
                @Bind("maxCapacity") int maxCap);


    @SqlQuery("SELECT * FROM materials WHERE id = :id")
    @RegisterRowMapper(MaterialMapper.class)
    public Material findById(long id);

    @SqlQuery("SELECT * FROM materials WHERE materialName = :materialName")
    @RegisterRowMapper(MaterialMapper.class)
    public Material findByExactName(String materialName);

    @SqlQuery("SELECT * FROM materials WHERE materialName LIKE :pattern")
    @RegisterRowMapper(MaterialMapper.class)
    List<Material> search(@Bind("pattern") String pattern);

    @SqlQuery("SELECT * FROM materials WHERE materialType = :materialType")
    @RegisterRowMapper(MaterialMapper.class)
    List<Material> findByMaterialType(@Bind("materialType") String materialType);

    @SqlQuery("SELECT * FROM materials")
    @RegisterRowMapper(MaterialMapper.class)
    public List<Material> listAll();


    class MaterialMapper implements RowMapper<Material> {
        @Override
        public Material map(ResultSet rs, StatementContext ctx) throws SQLException {
            Material material = new Material(
                    rs.getLong("id"),
                    rs.getString("materialName"),
                    rs.getString("materialType"),
                    rs.getInt("amount"),
                    rs.getInt("maxCapacity")
            );
            return material;
        }
    }

    record Material(long id, String materialName, String materialType, int amount, int maxCap) implements ToJsonConvertible {
        public String getMaterialName() {return materialName;}
        public String getMaterialType() {return materialType;}
        public int getAmount() {return amount;}
        public int getMaxCap() {return maxCap;}
        @Override public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    };
}


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

@RegisterRowMapper(MaterialsDao.MaterialMapper.class)
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


    @SqlQuery("SELECT * FROM materials WHERE materialName = :materialName")
    Material findByExactName(String materialName);

    @SqlQuery("SELECT * FROM materials WHERE materialName LIKE :pattern")
    List<Material> search(@Bind("pattern") String pattern);

    @SqlQuery("SELECT * FROM materials WHERE materialType = :materialType")
    List<Material> findByMaterialType(@Bind("materialType") String materialType);

    @SqlQuery("SELECT * FROM materials")
    List<Material> listAll();

    @SqlUpdate("DELETE FROM materials")
    void clear();


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

    class Material implements ToJsonConvertible {
        private long id;
        private String materialName;
        private String materialType;
        private int amount;
        private int maxCap;

        public Material(long id, String materialName, String materialType, int amount, int maxCap) {
            this.id = id;
            this.materialName = materialName;
            this.materialType = materialType;
            this.amount = amount;
            this.maxCap = maxCap;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getMaterialName() {
            return materialName;
        }

        public void setMaterialName(String materialName) {
            this.materialName = materialName;
        }

        public String getMaterialType() {
            return materialType;
        }

        public void setMaterialType(String materialType) {
            this.materialType = materialType;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getMaxCap() {
            return maxCap;
        }

        public void setMaxCap(int maxCap) {
            this.maxCap = maxCap;
        }

        @Override
        public String toJson() {
            return GsonFactory.getGson().toJson(this);
        }
    }
}


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

@RegisterRowMapper(CodexEntryDao.CodexEntryMapper.class)
public interface CodexEntryDao {

    @SqlUpdate("""
            INSERT INTO codex_entries(subCategory, starSystem, bodyId, latitude, longitude, entryName, voucherAmount)
                        values (:subCategory, :starSystem, :bodyId, :latitude, :longitude, :entryName, :voucherAmount)
            """)
    void save(@BindBean CodexEntry entry);


    @SqlUpdate("DELETE FROM codex_entries WHERE entryName = :entryName and starSystem = :starSystem and bodyId=:bodyId")
    void clearCompleted(@Bind String entryName, @Bind String starSystem, @Bind Long bodyId);


    @SqlQuery("SELECT * FROM codex_entries WHERE bodyId = :planetId AND starSystem = :starSystem")
    List<CodexEntry> getForPlanet(@Bind("planetId") Long planetId, @Bind("starSystem") String starSystem);


    @SqlQuery("SELECT EXISTS (SELECT 1 FROM codex_entries WHERE entryName = :entryName AND starSystem = :starSystem AND bodyId = :bodyId)")
    boolean contains(@Bind("entryName") String entryName, @Bind("starSystem") String starSystem, @Bind("bodyId") Long bodyId);

    @SqlUpdate("DELETE FROM codex_entries")
    void clear();

    @SqlQuery("SELECT * FROM codex_entries")
    List<CodexEntry> findAll();

    class CodexEntryMapper implements RowMapper<CodexEntry> {
        @Override public CodexEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
            CodexEntry entry = new CodexEntry();
            entry.setSubCategory(rs.getString("subCategory"));
            entry.setStarSystem(rs.getString("starSystem"));
            entry.setBodyId(rs.getLong("bodyId"));
            entry.setLatitude(rs.getDouble("latitude"));
            entry.setLongitude(rs.getDouble("longitude"));
            entry.setEntryName(rs.getString("entryName"));
            entry.setVoucherAmount(rs.getLong("voucherAmount"));
            return entry;
        }
    }


    class CodexEntry{
        private String subCategory;
        private String starSystem;
        private Long bodyId;
        private Double latitude;
        private Double longitude;
        private String entryName;
        private Long voucherAmount;


        public String getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(String subCategory) {
            this.subCategory = subCategory;
        }

        public String getStarSystem() {
            return starSystem;
        }

        public void setStarSystem(String starSystem) {
            this.starSystem = starSystem;
        }

        public Long getBodyId() {
            return bodyId;
        }

        public void setBodyId(Long bodyId) {
            this.bodyId = bodyId;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getEntryName() {
            return entryName;
        }

        public void setEntryName(String entryName) {
            this.entryName = entryName;
        }

        public Long getVoucherAmount() {
            return voucherAmount;
        }

        public void setVoucherAmount(Long voucherAmount) {
            this.voucherAmount = voucherAmount;
        }
    }
}

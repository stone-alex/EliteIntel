package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(GenusPaymentAnnouncementDao.GenusPaymentAnouncementMapper.class)
public interface GenusPaymentAnnouncementDao {

    @SqlUpdate("""
            INSERT OR REPLACE INTO genus_payment_announcement (genus, isOn)
            VALUES (:genus, :isOn)
            """)
    void upsert(@Bind("genus") String genus, @Bind("isOn") boolean isOn);


    @SqlQuery("SELECT * FROM genus_payment_announcement WHERE genus = :genus")
    GenusPaymentAnouncement get(@Bind("genus") String genus);

    @SqlUpdate("DELETE FROM genus_payment_announcement")
    void clear();


    class GenusPaymentAnouncementMapper implements RowMapper<GenusPaymentAnouncement> {

        @Override public GenusPaymentAnouncement map(ResultSet rs, StatementContext ctx) throws SQLException {

            GenusPaymentAnouncement anouncement = new GenusPaymentAnouncement();
            anouncement.setGenus(rs.getString("genus"));
            anouncement.setOn(rs.getBoolean("isOn"));
            return anouncement;
        }
    }


    class GenusPaymentAnouncement {

        private String genus;
        private Boolean isOn;

        public GenusPaymentAnouncement() {
        }

        public String getGenus() {
            return genus;
        }

        public void setGenus(String genus) {
            this.genus = genus;
        }

        public Boolean getOn() {
            return isOn;
        }

        public void setOn(Boolean on) {
            isOn = on;
        }
    }
}

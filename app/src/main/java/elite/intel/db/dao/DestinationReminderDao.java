package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(DestinationReminderDao.DestinationMapper.class)
public interface DestinationReminderDao {


    @SqlUpdate("INSERT OR REPLACE INTO destination_reminder (id, json) VALUES (1, :json)")
    void save(@BindBean DestinationReminderDao.Destination data);

    @SqlQuery("SELECT json FROM destination_reminder WHERE id = 1")
    DestinationReminderDao.Destination get();

    @SqlUpdate("DELETE FROM destination_reminder")
    void clear();

    class DestinationMapper implements RowMapper<DestinationReminderDao.Destination> {

        @Override public DestinationReminderDao.Destination map(ResultSet rs, StatementContext ctx) throws SQLException {
            Destination destination = new Destination();
            destination.setJson(rs.getString("json"));
            return destination;
        }
    }


    class Destination {
        public Destination() {
        }

        private String json;

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }
}

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

@RegisterRowMapper(DeferredNotificationDao.DeferredNotificationMapper.class)
public interface DeferredNotificationDao {

    @SqlQuery("""
            select * from deferred_notifications where timeToNotify <= :now
            """)
    List<DeferredNotification> get(@Bind("now") Long now);

    @SqlUpdate("""
            insert into deferred_notifications (key, timeToNotify, notification) values (:key, :timeToNotify, :notification)
            """)
    void insert(@BindBean DeferredNotification notification);

    @SqlUpdate("DELETE FROM deferred_notifications where key = :key")
    void delete(@Bind("key") String key);


    class DeferredNotificationMapper implements RowMapper<DeferredNotification> {

        @Override public DeferredNotification map(ResultSet rs, StatementContext ctx) throws SQLException {
            DeferredNotification entity = new DeferredNotification();
            entity.setKey(rs.getString("key"));
            entity.setTimeToNotify(rs.getLong("timeToNotify"));
            entity.setNotification(rs.getString("notification"));
            return entity;
        }
    }


    class DeferredNotification {
        String key;
        Long timeToNotify;
        String notification;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Long getTimeToNotify() {
            return timeToNotify;
        }

        public void setTimeToNotify(Long timeToNotify) {
            this.timeToNotify = timeToNotify;
        }

        public String getNotification() {
            return notification;
        }

        public void setNotification(String notification) {
            this.notification = notification;
        }
    }
}

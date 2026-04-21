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

@RegisterRowMapper(BindingConflictDao.ConflictMapper.class)
public interface BindingConflictDao {

    @SqlUpdate("INSERT OR IGNORE INTO binding_conflicts (conflict_key, description) VALUES (:conflictKey, :description)")
    void save(@BindBean ConflictRecord record);

    @SqlUpdate("DELETE FROM binding_conflicts WHERE conflict_key = :conflictKey")
    void remove(@Bind("conflictKey") String conflictKey);

    @SqlQuery("SELECT * FROM binding_conflicts")
    List<ConflictRecord> listAll();

    @SqlUpdate("DELETE FROM binding_conflicts")
    void clear();

    class ConflictMapper implements RowMapper<ConflictRecord> {
        @Override
        public ConflictRecord map(ResultSet rs, StatementContext ctx) throws SQLException {
            ConflictRecord r = new ConflictRecord();
            r.setConflictKey(rs.getString("conflict_key"));
            r.setDescription(rs.getString("description"));
            return r;
        }
    }

    class ConflictRecord {
        private String conflictKey;
        private String description;

        public String getConflictKey() {
            return conflictKey;
        }

        public void setConflictKey(String conflictKey) {
            this.conflictKey = conflictKey;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

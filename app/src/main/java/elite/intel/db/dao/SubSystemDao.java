package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RegisterRowMapper(SubSystemDao.SubSystemMapper.class)
public interface SubSystemDao {

    @SqlQuery("SELECT subsystem FROM sub_system ORDER BY subsystem")
    List<String> getAllNamesLowerCase();

    @SqlQuery("SELECT subsystem FROM sub_system WHERE LOWER(subsystem) = LOWER(:subsystem) LIMIT 1")
    String getOriginalCase(@Bind("subsystem") String subsystem);


    class SubSystemMapper implements RowMapper<SubSystem>{

        @Override public SubSystem map(ResultSet rs, StatementContext ctx) throws SQLException {
            SubSystem system = new SubSystem();
            system.setSubsystem(rs.getString("subsystem"));
            return system;
        }
    }

    class SubSystem {
        private String subsystem;

        public String getSubsystem() {
            return subsystem;
        }

        public void setSubsystem(String subsystem) {
            this.subsystem = subsystem;
        }
    }
}

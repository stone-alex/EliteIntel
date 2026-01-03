package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RegisterRowMapper(HelpDao.HelpEntityMapper.class)
public interface HelpDao {

    @SqlQuery("""
            SELECT * FROM help_topics where LOWER(topic) like '%' || LOWER(:topic) || '%' or LOWER(help) like '%' || LOWER(:pattern) || '%' 
            """)
    List<HelpEntity> getHelp(@Bind("topic") String topic, @Bind("pattern") String pattern);

    class HelpEntityMapper implements RowMapper<HelpEntity>{

        @Override public HelpEntity map(ResultSet rs, StatementContext ctx) throws SQLException {
            HelpEntity helpEntity = new HelpEntity();
            helpEntity.setTopic(rs.getString("topic"));
            helpEntity.setHelpText(rs.getString("help"));
            return helpEntity;
        }
    }


    class HelpEntity {
        private String topic;
        private String helpText;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getHelpText() {
            return helpText;
        }

        public void setHelpText(String helpText) {
            this.helpText = helpText;
        }
    }
}

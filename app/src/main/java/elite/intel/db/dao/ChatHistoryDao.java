package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(ChatHistoryDao.ChatHistoryMapper.class)
public interface ChatHistoryDao {

    @SqlUpdate("INSERT OR REPLACE INTO chat_history (json, timestamp) VALUES (:json, :timestamp)")
    void save(@BindBean ChatHistoryDao.ChatHistory data);

    @SqlUpdate("DELETE FROM chat_history")
    void clear();

    @SqlQuery("SELECT json, timestamp FROM chat_history")
    ChatHistory[] listAll();


    class ChatHistoryMapper implements RowMapper<ChatHistory> {
        @Override public ChatHistory map(ResultSet rs, StatementContext ctx) throws SQLException {
            ChatHistory chatHistory = new ChatHistory();
            chatHistory.setJson(rs.getString("json"));
            chatHistory.setTimestamp(rs.getString("timestamp"));
            return chatHistory;
        }
    }


    class ChatHistory {
        private String json;
        private String timestamp;
        public ChatHistory() {
        }

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}

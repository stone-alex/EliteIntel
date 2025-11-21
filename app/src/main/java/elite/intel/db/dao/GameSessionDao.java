package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(GameSessionDao.GameSessionMapper.class)
public interface GameSessionDao {


    @SqlUpdate("""
            INSERT OR REPLACE INTO game_session (id, aiPersonality,  aiCadence, aiVoice, aiApiKey, ttsApiKey, sttApiKey, loggingEnabled, privacyModeOn, rmsThresholdHigh,  rmsThresholdLow)
                                  VALUES (1, :aiPersonality, :aiCadence, :aiVoice, :aiApiKey, :ttsApiKey, :sttApiKey, :loggingEnabled, :privacyModeOn, :rmsThresholdHigh, :rmsThresholdLow)
            """)
    void save(@BindBean GameSessionDao.GameSession data);

    @SqlQuery("SELECT * FROM game_session WHERE id = 1")
    GameSession get();


    class GameSessionMapper implements RowMapper<GameSessionDao.GameSession> {

        @Override public GameSession map(ResultSet rs, StatementContext ctx) throws SQLException {
            GameSession session = new GameSession();
            session.setAiPersonality(rs.getString("aiPersonality"));
            session.setAiApiKey(rs.getString("aiApiKey"));
            session.setAiCadence(rs.getString("aiCadence"));
            session.setTtsApiKey(rs.getString("ttsApiKey"));
            session.setSttApiKey(rs.getString("sttApiKey"));
            session.setLoggingEnabled(rs.getBoolean("loggingEnabled"));
            session.setAiVoice(rs.getString("aiVoice"));
            session.setPrivacyModeOn(rs.getBoolean("privacyModeOn"));
            session.setRmsThresholdHigh(rs.getDouble("rmsThresholdHigh"));
            session.setRmsThresholdLow(rs.getDouble("rmsThresholdLow"));

            return session;
        }
    }


    class GameSession {
        private String aiPersonality;
        private String aiApiKey;
        private String aiCadence;
        private String ttsApiKey;
        private String sttApiKey;
        private Boolean loggingEnabled;
        private String aiVoice;
        private Boolean privacyModeOn;
        private Double rmsThresholdHigh = 460.00;
        private Double rmsThresholdLow = 100.00;


        public String getAiPersonality() {
            return aiPersonality;
        }

        public void setAiPersonality(String aiPersonality) {
            this.aiPersonality = aiPersonality;
        }

        public String getAiApiKey() {
            return aiApiKey;
        }

        public void setAiApiKey(String aiApiKey) {
            this.aiApiKey = aiApiKey;
        }

        public String getAiCadence() {
            return aiCadence;
        }

        public void setAiCadence(String aiCadence) {
            this.aiCadence = aiCadence;
        }

        public String getTtsApiKey() {
            return ttsApiKey;
        }

        public void setTtsApiKey(String ttsApiKey) {
            this.ttsApiKey = ttsApiKey;
        }

        public String getSttApiKey() {
            return sttApiKey;
        }

        public void setSttApiKey(String sttApiKey) {
            this.sttApiKey = sttApiKey;
        }

        public Boolean getLoggingEnabled() {
            return loggingEnabled;
        }

        public void setLoggingEnabled(Boolean loggingEnabled) {
            this.loggingEnabled = loggingEnabled;
        }

        public String getAiVoice() {
            return aiVoice;
        }

        public void setAiVoice(String aiVoice) {
            this.aiVoice = aiVoice;
        }

        public Boolean getPrivacyModeOn() {
            return privacyModeOn;
        }

        public void setPrivacyModeOn(Boolean privacyModeOn) {
            this.privacyModeOn = privacyModeOn;
        }

        public Double getRmsThresholdLow() {
            return rmsThresholdLow;
        }

        public void setRmsThresholdLow(Double rmsThresholdLow) {
            this.rmsThresholdLow = rmsThresholdLow;
        }

        public Double getRmsThresholdHigh() {
            return rmsThresholdHigh;
        }

        public void setRmsThresholdHigh(Double rmsThresholdHigh) {
            this.rmsThresholdHigh = rmsThresholdHigh;
        }
    }
}

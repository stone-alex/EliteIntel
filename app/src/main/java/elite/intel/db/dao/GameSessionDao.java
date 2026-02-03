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
            INSERT OR REPLACE INTO game_session (id, aiPersonality,  aiCadence, aiVoice, aiApiKey, ttsApiKey, sttApiKey, 
                                                             edsmApiKey, loggingEnabled, privacyModeOn, rmsThresholdHigh,  
                                                             rmsThresholdLow, encryptedLLMKey, encryptedSTTKey, encryptedTTSKey, 
                                                             encryptedEDSSMKey, sendMarketData, sendOutfittingData, sendShipyardData, 
                                                             sendExplorationData, speechSpeed, localLlmCommandModel, localLlmQueryModel,
                                                             useLocalCommandLlm, useLocalQueryLlm, useLocalTTS
                                                )
                                  VALUES (1, :aiPersonality, :aiCadence, :aiVoice, :aiApiKey, :ttsApiKey, :sttApiKey, 
                                                      :edsmApiKey, :loggingEnabled, :privacyModeOn, :rmsThresholdHigh, 
                                                      :rmsThresholdLow, :encryptedLLMKey, :encryptedSTTKey, :encryptedTTSKey, 
                                                      :encryptedEDSSMKey, :sendMarketData, :sendOutfittingData, :sendShipyardData, 
                                                      :sendExplorationData, :speechSpeed, :localLlmCommandModel, :localLlmQueryModel,
                                                      :useLocalCommandLlm, :useLocalQueryLlm, :useLocalTTS
                                          )
            """)
    void save(@BindBean GameSessionDao.GameSession data);

    @SqlQuery("SELECT * FROM game_session WHERE id = 1")
    GameSession get();


    class GameSessionMapper implements RowMapper<GameSessionDao.GameSession> {

        @Override public GameSession map(ResultSet rs, StatementContext ctx) throws SQLException {
            GameSession session = new GameSession();
            session.setAiPersonality(rs.getString("aiPersonality"));
            session.setAiCadence(rs.getString("aiCadence"));

            /// >> DEPRECATED
            session.setAiApiKey(rs.getString("aiApiKey"));
            session.setTtsApiKey(rs.getString("ttsApiKey"));
            session.setSttApiKey(rs.getString("sttApiKey"));
            /// <<

            session.setEncryptedLLMKey(rs.getString("encryptedLLMKey"));
            session.setEncryptedSTTKey(rs.getString("encryptedSTTKey"));
            session.setEncryptedTTSKey(rs.getString("encryptedTTSKey"));
            session.setEncryptedEDSSMKey(rs.getString("encryptedEDSSMKey"));

            session.setLoggingEnabled(rs.getBoolean("loggingEnabled"));
            session.setAiVoice(rs.getString("aiVoice"));
            session.setPrivacyModeOn(rs.getBoolean("privacyModeOn"));
            session.setRmsThresholdHigh(rs.getDouble("rmsThresholdHigh"));
            session.setRmsThresholdLow(rs.getDouble("rmsThresholdLow"));
            session.setEdsmApiKey(rs.getString("edsmApiKey"));

            session.setSendMarketData(rs.getBoolean("sendMarketData"));
            session.setSendOutfittingData(rs.getBoolean("sendOutfittingData"));
            session.setSendShipyardData(rs.getBoolean("sendShipyardData"));
            session.setSendExplorationData(rs.getBoolean("sendExplorationData"));
            session.setLocalLlmCommandModel(rs.getString("localLlmCommandModel"));
            session.setLocalLlmQueryModel(rs.getString("localLlmQueryModel"));
            session.setSpeechSpeed(rs.getFloat("speechSpeed"));

            session.setUseLocalCommandLlm(rs.getBoolean("useLocalCommandLlm"));
            session.setUseLocalQueryLlm(rs.getBoolean("useLocalQueryLlm"));
            session.setUseLocalTTS(rs.getBoolean("useLocalTTS"));
            return session;
        }
    }


    class GameSession {
        private String aiPersonality;
        private String aiCadence;

        private String aiApiKey;
        private String ttsApiKey;
        private String sttApiKey;

        private String encryptedLLMKey;
        private String encryptedSTTKey;
        private String encryptedTTSKey;
        private String encryptedEDSSMKey;

        private Boolean loggingEnabled;
        private String aiVoice;
        private Boolean privacyModeOn;
        private Double rmsThresholdHigh = 460.00;
        private Double rmsThresholdLow = 100.00;
        private String edsmApiKey;

        private Boolean sendMarketData;
        private Boolean sendOutfittingData;
        private Boolean sendShipyardData;
        private Boolean sendExplorationData;
        private Float speechSpeed;
        private String localLlmCommandModel;
        private String localLlmQueryModel;

        private boolean useLocalCommandLlm;
        private boolean useLocalQueryLlm;
        private boolean useLocalTTS;


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

        public void setEdsmApiKey(String edsmApiKey) {
            this.edsmApiKey = edsmApiKey;
        }

        public String getEdsmApiKey() {
            return edsmApiKey;
        }

        public String getEncryptedLLMKey() {
            return encryptedLLMKey;
        }

        public void setEncryptedLLMKey(String encryptedLLMKey) {
            this.encryptedLLMKey = encryptedLLMKey;
        }

        public String getEncryptedSTTKey() {
            return encryptedSTTKey;
        }

        public void setEncryptedSTTKey(String encryptedSTTKey) {
            this.encryptedSTTKey = encryptedSTTKey;
        }

        public String getEncryptedTTSKey() {
            return encryptedTTSKey;
        }

        public void setEncryptedTTSKey(String encryptedTTSKey) {
            this.encryptedTTSKey = encryptedTTSKey;
        }

        public String getEncryptedEDSSMKey() {
            return encryptedEDSSMKey;
        }

        public void setEncryptedEDSSMKey(String encryptedEDSSMKey) {
            this.encryptedEDSSMKey = encryptedEDSSMKey;
        }


        public Boolean getSendMarketData() {
            return sendMarketData;
        }

        public void setSendMarketData(Boolean sendMarketData) {
            this.sendMarketData = sendMarketData;
        }

        public Boolean getSendOutfittingData() {
            return sendOutfittingData;
        }

        public void setSendOutfittingData(Boolean sendOutfittingData) {
            this.sendOutfittingData = sendOutfittingData;
        }

        public Boolean getSendShipyardData() {
            return sendShipyardData;
        }

        public void setSendShipyardData(Boolean sendShipyardData) {
            this.sendShipyardData = sendShipyardData;
        }

        public Boolean getSendExplorationData() {
            return sendExplorationData;
        }

        public void setSendExplorationData(Boolean sendExplorationData) {
            this.sendExplorationData = sendExplorationData;
        }

        public Float getSpeechSpeed() {
            return speechSpeed;
        }

        public void setSpeechSpeed(Float speechSpeed) {
            this.speechSpeed = speechSpeed;
        }

        public String getLocalLlmCommandModel() {
            return localLlmCommandModel;
        }

        public void setLocalLlmCommandModel(String localLlmCommandModel) {
            this.localLlmCommandModel = localLlmCommandModel;
        }

        public String getLocalLlmQueryModel() {
            return localLlmQueryModel;
        }

        public void setLocalLlmQueryModel(String localLlmQueryModel) {
            this.localLlmQueryModel = localLlmQueryModel;
        }


        public boolean isUseLocalCommandLlm() {
            return useLocalCommandLlm;
        }

        public void setUseLocalCommandLlm(boolean useLocalCommandLlm) {
            this.useLocalCommandLlm = useLocalCommandLlm;
        }

        public boolean isUseLocalQueryLlm() {
            return useLocalQueryLlm;
        }

        public void setUseLocalQueryLlm(boolean useLocalQueryLlm) {
            this.useLocalQueryLlm = useLocalQueryLlm;
        }

        public boolean isUseLocalTTS() {
            return useLocalTTS;
        }

        public void setUseLocalTTS(boolean useLocalTTS) {
            this.useLocalTTS = useLocalTTS;
        }
    }
}

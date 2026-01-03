package elite.intel.session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.mouth.AiVoices;
import elite.intel.db.dao.ChatHistoryDao;
import elite.intel.db.dao.GameSessionDao;
import elite.intel.db.util.Database;
import elite.intel.util.Cypher;
import elite.intel.util.json.GsonFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SystemSession {

    // Config keys
    public static final String AI_API_KEY = "ai_api_key";
    public static final String EDSM_KEY = "edsm_api_key";
    public static final String TTS_API_KEY = "tts_api_key"; // New key for Google API
    public static final String STT_API_KEY = "stt_api_key";

    public static final String DEBUG_SWITCH = "logging_enabled";


    private static volatile SystemSession instance;

    private SystemSession() {
    }


    public static SystemSession getInstance() {
        if (instance == null) {
            synchronized (Status.class) {
                if (instance == null) {
                    instance = new SystemSession();
                }
            }
        }
        return instance;
    }


    public AiVoices getAIVoice() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            if (session == null) return AiVoices.STEVE;
            if (session.getAiVoice() == null) return AiVoices.STEVE;
            return AiVoices.valueOf(
                    session.getAiVoice()
            );
        });
    }

    public void setAIVoice(AiVoices voice) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setAiVoice(voice.name());
            dao.save(session);
            return null;
        });
    }

    public void setAIPersonality(AIPersonality personality) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setAiPersonality(personality.name());
            dao.save(session);
            return null;
        });
    }

    public AIPersonality getAIPersonality() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            if (session == null) return AIPersonality.CASUAL;
            if (session.getAiPersonality() == null) return AIPersonality.CASUAL;
            return AIPersonality.valueOf(
                    session.getAiPersonality()
            );
        });
    }

    public void setAICadence(AICadence cadence) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setAiCadence(cadence.name());
            dao.save(session);
            return null;
        });
    }

    public AICadence getAICadence() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            if (session == null) return AICadence.FEDERATION;
            if (session.getAiCadence() == null) return AICadence.FEDERATION;
            return AICadence.valueOf(
                    session.getAiCadence()
            );
        });
    }

    public JsonArray getChatHistory() {
        return Database.withDao(ChatHistoryDao.class, dao -> {
            ChatHistoryDao.ChatHistory[] chats = dao.listAll();
            JsonArray result = new JsonArray();
            if (chats == null) {
                return result;
            }
            for (ChatHistoryDao.ChatHistory chat : chats) {
                String json = chat.getJson();
                result.add(GsonFactory.getGson().fromJson(json, JsonObject.class));
            }
            return result;
        });
    }

    public void setChatHistory(JsonArray chatHistory) {
        Database.withDao(ChatHistoryDao.class, dao -> {
            ChatHistoryDao.ChatHistory data = new ChatHistoryDao.ChatHistory();
            data.setJson(chatHistory.toString());
            dao.save(data);
            return null;
        });
    }

    public void appendToChatHistory(JsonObject userMessage, JsonObject assistantMessage) {
        Database.withDao(ChatHistoryDao.class, dao -> {
            ChatHistoryDao.ChatHistory userChat = new ChatHistoryDao.ChatHistory();
            userChat.setJson(userMessage.toString());
            ChatHistoryDao.ChatHistory assistantChat = new ChatHistoryDao.ChatHistory();
            assistantChat.setJson(assistantMessage.toString());
            dao.save(userChat);
            dao.save(assistantChat);
            return null;
        });
    }

    public void clearChatHistory() {
        Database.withDao(ChatHistoryDao.class, dao -> {
            dao.clear();
            return null;
        });
    }


    public boolean isStreamingModeOn() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            return session.getPrivacyModeOn();
        });
    }

    public void setStreamingMode(boolean streamingModeOn) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setPrivacyModeOn(streamingModeOn);
            dao.save(session);
            return null;
        });
    }


    public Double getRmsThresholdHigh() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            return session.getRmsThresholdHigh();
        });
    }

    public void setRmsThresholdHigh(Double rmsThresholdHigh) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setRmsThresholdHigh(rmsThresholdHigh == null ? 0.0 : rmsThresholdHigh);
            dao.save(session);
            return null;
        });
    }

    public Double getRmsThresholdLow() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            return session.getRmsThresholdLow();
        });
    }

    public void setRmsThresholdLow(Double rmsThresholdLow) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setRmsThresholdLow(rmsThresholdLow == null ? 0.0 : rmsThresholdLow);
            dao.save(session);
            return null;
        });
    }

    // New getters and setters
    public boolean isLoggingEnabled() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            return session.getLoggingEnabled();
        });
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setLoggingEnabled(loggingEnabled);
            dao.save(session);
            return null;
        });
    }

    public String getTtsApiKey() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            return Cypher.decrypt(session.getEncryptedTTSKey());
        });
    }

    public boolean isRunningPiperTts() {
        String ttsApiKey = getTtsApiKey();
        return ttsApiKey == null || ttsApiKey.isEmpty();
    }

    public void setTtsApiKey(String ttsApiKey) {
        if (ttsApiKey == null && ttsApiKey.isEmpty()) {
            Database.withDao(GameSessionDao.class, dao -> {
                GameSessionDao.GameSession session = dao.get();
                session.setTtsApiKey(null);
                dao.save(session);
                return Void.class;
            });
        } else {
            Database.withDao(GameSessionDao.class, dao -> {
                GameSessionDao.GameSession session = dao.get();
                session.setEncryptedTTSKey(Cypher.encrypt(ttsApiKey));
                dao.save(session);
                return null;
            });
        }
    }

    public String getSttApiKey() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            return Cypher.decrypt(session.getEncryptedSTTKey());
        });
    }

    public void setSttApiKey(String sttApiKey) {
        if (sttApiKey == null && sttApiKey.isEmpty()) {
            Database.withDao(GameSessionDao.class, dao -> {
                GameSessionDao.GameSession session = dao.get();
                session.setSttApiKey(null);
                dao.save(session);
                return Void.class;
            });
        } else {
            Database.withDao(GameSessionDao.class, dao -> {
                GameSessionDao.GameSession session = dao.get();
                session.setEncryptedSTTKey(Cypher.encrypt(sttApiKey));
                dao.save(session);
                return null;
            });
        }
    }

    public String getAiApiKey() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            return Cypher.decrypt(session.getEncryptedLLMKey());
        });
    }

    public void setAiApiKey(String aiApiKey) {
        if (aiApiKey == null && aiApiKey.isEmpty()) {
            Database.withDao(GameSessionDao.class, dao -> {
                GameSessionDao.GameSession session = dao.get();
                session.setAiApiKey(null);
                dao.save(session);
                return Void.class;
            });
        } else {
            Database.withDao(GameSessionDao.class, dao -> {
                GameSessionDao.GameSession session = dao.get();
                session.setEncryptedLLMKey(Cypher.encrypt(aiApiKey));
                dao.save(session);
                return null;
            });
        }
    }

    public void setEdsmApiKey(String edsmApiKey) {
        if (edsmApiKey == null && edsmApiKey.isEmpty()) {
            Database.withDao(GameSessionDao.class, dao -> {
                GameSessionDao.GameSession session = dao.get();
                session.setEdsmApiKey(null);
                dao.save(session);
                return Void.class;
            });
        } else {
            Database.withDao(GameSessionDao.class, dao -> {
                GameSessionDao.GameSession session = dao.get();
                session.setEncryptedEDSSMKey(Cypher.encrypt(edsmApiKey));
                dao.save(session);
                return Void.class;
            });
        }
    }

    public String getEdsmApiKey() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            String oldKey = session.getEdsmApiKey();
            if (oldKey == null) {
                return Cypher.decrypt(session.getEncryptedEDSSMKey());
            } else {
                session.setEncryptedEDSSMKey(Cypher.encrypt(oldKey));
                session.setEdsmApiKey(null);
                dao.save(session);
                return Cypher.decrypt(session.getEncryptedEDSSMKey());
            }
        });
    }

    public Map<String, String> asMap() {
        Map<String, String> result = new HashMap<>();
        result.put(AI_API_KEY, getAiApiKey());
        result.put(EDSM_KEY, getEdsmApiKey());
        result.put(TTS_API_KEY, getTtsApiKey());
        result.put(STT_API_KEY, getSttApiKey());
        result.put(DEBUG_SWITCH, String.valueOf(isLoggingEnabled()));
        return result;
    }

    public void setSendMarketData(boolean enabled) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setSendMarketData(enabled);
            dao.save(session);
            return Void.class;
        });
    }

    public void setSendOutfittingData(boolean enabled) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setSendOutfittingData(enabled);
            dao.save(session);
            return Void.class;
        });
    }

    public void setSendShipyardDataEvent(boolean enabled) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setSendShipyardData(enabled);
            dao.save(session);
            return Void.class;
        });
    }

    public void setExplorationData(boolean enabled) {
        Database.withDao(GameSessionDao.class, dao ->{
            GameSessionDao.GameSession gameSession = dao.get();
            gameSession.setSendExplorationData(enabled);
            dao.save(gameSession);
            return Void.class;
        });
    }


    public boolean isExplorationData() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getSendExplorationData());
    }

    public boolean isSendMarketData() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getSendMarketData());
    }

    public boolean isSendOutfittingData() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getSendOutfittingData());
    }

    public boolean isSendShipyardData() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getSendShipyardData());
    }

    public String readVersionFromResources() {
        try {
            InputStream is = getClass().getResourceAsStream("/version.txt");
            return new BufferedReader(new InputStreamReader(is)).readLine();
        } catch (IOException e) {
            return "Unknown";
        }
    }

    public boolean isSendExplorationData() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getSendExplorationData());
    }
}
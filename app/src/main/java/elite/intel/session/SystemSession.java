package elite.intel.session;

import elite.intel.ai.brain.AICadence;
import elite.intel.ai.brain.AIPersonality;
import elite.intel.ai.mouth.GoogleVoices;
import elite.intel.ai.mouth.kokoro.KokoroVoices;
import elite.intel.db.dao.ChatHistoryDao;
import elite.intel.db.dao.GameSessionDao;
import elite.intel.db.dao.ShipDao;
import elite.intel.db.managers.ShipManager;
import elite.intel.db.util.Database;
import elite.intel.util.AppPaths;
import elite.intel.util.Cypher;
import elite.intel.util.json.GsonFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SystemSession {
    private static volatile SystemSession instance;
    private final ShipManager shipManager = ShipManager.getInstance();

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


    public GoogleVoices getGoogleVoice() {
        ShipDao.Ship ship = ShipManager.getInstance().getShip();
        if (ship == null) return GoogleVoices.STEVE;
        String voice = ship.getVoice();
        if (voice == null) return GoogleVoices.STEVE;
        return GoogleVoices.valueOf(voice);
    }


    public KokoroVoices getKokoroVoice() {
        ShipDao.Ship ship = ShipManager.getInstance().getShip();
        if (ship == null) return KokoroVoices.EMMA;
        String voice = ship.getVoice();
        if (voice == null) return KokoroVoices.EMMA;
        return KokoroVoices.valueOf(voice);
    }


    public void setGoogleVoice(GoogleVoices voice) {
        setShipVoice(voice.name());
    }

    private void setShipVoice(String voice) {
        ShipManager shipManager = ShipManager.getInstance();
        ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return;
        ship.setVoice(voice);
        shipManager.saveShip(ship);
    }

    public void setKokoroVoice(KokoroVoices voice) {
        setShipVoice(voice.name());
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
        ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return AIPersonality.CASUAL;
        return AIPersonality.valueOf(ship.getPersonality());
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
        ShipDao.Ship ship = shipManager.getShip();
        if (ship == null) return AICadence.IMPERIAL;
        return AICadence.valueOf(ship.getCadence());
    }

    public ChatHistory getChatHistory() {
        return Database.withDao(ChatHistoryDao.class, dao -> {
            ChatHistoryDao.ChatHistory chats = dao.lastChat();
            if (chats == null) {
                return new ChatHistory();
            }
            return GsonFactory.getGson().fromJson(chats.getJson(), ChatHistory.class);
        });
    }

    public void setChatHistory(ChatHistory chatHistory) {
        Database.withDao(ChatHistoryDao.class, dao -> {
            dao.clear();
            ChatHistoryDao.ChatHistory data = new ChatHistoryDao.ChatHistory();
            data.setJson(chatHistory.toJson());
            dao.save(data);
            return null;
        });
    }


    public boolean isStreamingModeOn() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            return session.getPrivacyModeOn();
        });
    }

    public void stopStartListening(boolean streamingModeOn) {
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


    public String getTtsApiKey() {
        return Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            return Cypher.decrypt(session.getEncryptedTTSKey());
        });
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


    public String readVersionFromResources() {
        try {
            InputStream is = getClass().getResourceAsStream("/version.txt");
            return new BufferedReader(new InputStreamReader(is)).readLine();
        } catch (IOException e) {
            return "Unknown";
        }
    }


    public void setSpeechSpeed(float speed) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setSpeechSpeed(speed);
            dao.save(session);
            return Void.class;
        });
    }

    public void setBeepVolume(float volume) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setNotificationVolume(volume);
            dao.save(session);
            return Void.class;
        });
    }

    public float getBeepVolume() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getNotificationVolume());
    }

    public Float getSpeechSpeed() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getSpeechSpeed());
    }

    public int getSttThreads() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getSttThreads());
    }

    public void setSttThreads(int threads) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setSttThreads(threads);
            dao.save(session);
            return Void.class;
        });
    }


    public void setLocalLlmCommandModel(String text) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setLocalLlmCommandModel(text);
            dao.save(session);
            return Void.class;
        });
    }

    public void setLocalLlmQueryModel(String text) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setLocalLlmQueryModel(text);
            dao.save(session);
            return Void.class;
        });
    }


    public void setUseLocalCommandLlm(boolean b) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setUseLocalCommandLlm(b);
            dao.save(session);
            return Void.class;
        });
    }

    public void setUseLocalQueryLlm(boolean b) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setUseLocalQueryLlm(b);
            dao.save(session);
            return Void.class;
        });
    }

    public void setUseLocalTTS(boolean b) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setUseLocalTTS(b);
            dao.save(session);
            return Void.class;
        });
    }


    public boolean useLocalCommandLlm() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().isUseLocalCommandLlm());
    }


    public boolean useLocalQueryLlm() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().isUseLocalQueryLlm());
    }

    public boolean useLocalTTS() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().isUseLocalTTS());
    }

    public String getLocalLlmCommandModel() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getLocalLlmCommandModel());
    }

    public String getLocalLlmQueryModel() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getLocalLlmQueryModel());
    }

    public String getWhisperModelPath() {
        return AppPaths.getWhisperModelPath().toString();
    }

    public String getDesignation() {
        ShipDao.Ship ship = shipManager.getShip();
        return ship == null ? "I have no designation" : ship.getShipName();
    }


    /// 0 to 100 %
    public int getVoiceVolume() {
        return Database.withDao(GameSessionDao.class, dao -> dao.get().getVoiceVolume());
    }

    public void setVoiceVolume(int volume) {
        Database.withDao(GameSessionDao.class, dao -> {
            GameSessionDao.GameSession session = dao.get();
            session.setVoiceVolume(volume);
            dao.save(session);
            return null;
        });
    }
}
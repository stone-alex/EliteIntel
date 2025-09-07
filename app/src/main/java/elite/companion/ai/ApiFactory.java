package elite.companion.ai;

import elite.companion.ai.brain.*;
import elite.companion.ai.brain.grok.*;
import elite.companion.ai.ears.EarsInterface;
import elite.companion.ai.ears.google.GoogleSTTImpl;
import elite.companion.ai.mouth.MouthInterface;
import elite.companion.ai.mouth.google.GoogleTTSImpl;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.ui.event.AppLogEvent;

import java.util.function.Supplier;

/**
 * A singleton factory class responsible for providing various AI-related endpoint instances.
 * The instances provided include LLM, STT, TTS, and other AI services based on API keys
 * specified in the application configuration.
 */
public class ApiFactory {
    private static ApiFactory instance;

    /**
     * Provides LLM/STT/TTS endpoint instances based on the API key provided in the config file.
     *
     */
    private ApiFactory() {
        // Prevent instantiation.
    }

    public static synchronized ApiFactory getInstance() {
        if (instance == null) instance = new ApiFactory();
        return instance;
    }

    public AiAnalysisInterface getAnalysisEndpoint() {
        return getAiImpl(ConfigManager.AI_API_KEY, "LLM", GrokAnalysisEndpoint::getInstance);
    }

    public AIChatInterface getChatEndpoint() {
        return getAiImpl(ConfigManager.AI_API_KEY, "LLM", GrokChatEndPoint::getInstance);
    }

    public AiContextFactory getAiContextFactory() {
        return getAiImpl(ConfigManager.AI_API_KEY, "LLM", GrokContextFactory::getInstance);
    }

    public AiCommandInterface getCommandEndpoint() {
        return getAiImpl(ConfigManager.AI_API_KEY, "LLM", GrokCommandEndPoint::getInstance);
    }

    public AiQueryInterface getQueryEndpoint() {
        return getAiImpl(ConfigManager.AI_API_KEY, "LLM", GrokQueryEndPoint::getInstance);
    }

    public AIRouterInterface getAiRouter() {
        return getAiImpl(ConfigManager.AI_API_KEY, "LLM", GrokResponseRouter::getInstance);
    }

    public MouthInterface getMouthImpl() {
        String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.TTS_API_KEY);
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "TTS");
        switch (provider) {
            case GOOGLE_TTS:
                return GoogleTTSImpl.getInstance();
            // TODO: Add ElevenLabs, AWS Polly, etc.
            default:
                EventBusManager.publish(new AppLogEvent("Unknown TTS key format"));
                EventBusManager.publish(new VoiceProcessEvent("Using default Google TTS—confirm?"));
                return GoogleTTSImpl.getInstance();
        }
    }

    public EarsInterface getEarsImpl() {
        String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.STT_API_KEY);
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "STT"); // Fixed category
        switch (provider) {
            case GOOGLE_STT:
                return new GoogleSTTImpl();
            // TODO: Add Deepgram, Azure STT, etc.
            default:
                EventBusManager.publish(new AppLogEvent("Unknown STT key format"));
                EventBusManager.publish(new VoiceProcessEvent("Using default Google STT—confirm?"));
                return new GoogleSTTImpl();
        }
    }

    private <T> T getAiImpl(String keyType, String category, Supplier<T> defaultSupplier) {
        String apiKey = ConfigManager.getInstance().getSystemKey(keyType);
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, category);
        switch (provider) {
            case GROK:
                return defaultSupplier.get();
            // TODO: Add OpenAI, Anthropic, etc.
            default:
                EventBusManager.publish(new AppLogEvent("Unknown AI key format"));
                EventBusManager.publish(new VoiceProcessEvent("Using default Grok AI—select provider?"));
                return defaultSupplier.get();
        }
    }
}
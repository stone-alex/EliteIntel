package elite.intel.ai;

import elite.intel.ai.brain.*;
import elite.intel.ai.brain.openai.*;
import elite.intel.ai.brain.xai.*;
import elite.intel.ai.ears.EarsInterface;
import elite.intel.ai.ears.google.GoogleSTTImpl;
import elite.intel.ai.mouth.MouthInterface;
import elite.intel.ai.mouth.google.GoogleTTSImpl;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.VoiceProcessEvent;
import elite.intel.ui.event.AppLogEvent;

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
        String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY);
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokAnalysisEndpoint.getInstance();
            case OPENAI -> OpenAiAnalysisEndPoint.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };

    }

    public AIChatInterface getChatEndpoint() {
        String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY);
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokChatEndPoint.getInstance();
            case OPENAI -> OpenAiChatEndPoint.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };
    }

    public AiContextFactory getAiContextFactory() {
        String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY);
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokContextFactory.getInstance();
            case OPENAI -> OpenAiContextFactory.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };
    }

    public AiCommandInterface getCommandEndpoint() {
        String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY);
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokCommandEndPoint.getInstance();
            case OPENAI -> OpenAiCommandEndPoint.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };
    }

    public AiQueryInterface getQueryEndpoint() {
        String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY);
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokAiEndPoint.getInstance();
            case OPENAI -> OpenAiAiEndPoint.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };
    }

    public AIRouterInterface getAiRouter() {
        String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY);
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokResponseRouter.getInstance();
            case OPENAI -> OpenAiResponseRouter.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };
    }

    ///

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

    ///

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
}
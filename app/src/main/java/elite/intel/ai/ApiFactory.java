package elite.intel.ai;

import elite.intel.ai.brain.*;
import elite.intel.ai.brain.commons.CommonAiPromptFactory;
import elite.intel.ai.brain.openai.*;
import elite.intel.ai.brain.xai.*;
import elite.intel.ai.ears.EarsInterface;
import elite.intel.ai.ears.google.GoogleSTTImpl;
import elite.intel.ai.mouth.MouthInterface;
import elite.intel.ai.mouth.google.GoogleTTSImpl;
import elite.intel.ai.mouth.piper.PiperTTS;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
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
        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokAnalysisEndpoint.getInstance();
            case OPENAI -> OpenAiAnalysisEndPoint.getInstance();
            //TODO: replace default with Local LLM End Points.
            default -> throw new IllegalStateException("Unknown AI key format");
        };

    }

    public AIChatInterface getChatEndpoint() {
        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokChatEndPoint.getInstance();
            case OPENAI -> OpenAiChatEndPoint.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };
    }

    public AiPromptFactory getAiPromptFactory() {
        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> CommonAiPromptFactory.getInstance();
            case OPENAI -> CommonAiPromptFactory.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };
    }

    public AiCommandInterface getCommandEndpoint() {
        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokCommandEndPoint.getInstance();
            case OPENAI -> OpenAiCommandEndPoint.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };
    }

    public AiQueryInterface getQueryEndpoint() {
        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokQueryEndPoint.getInstance();
            case OPENAI -> OpenAiQueryEndPoint.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };
    }

    public AIRouterInterface getAiRouter() {
        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokResponseRouter.getInstance();
            case OPENAI -> OpenAiResponseRouter.getInstance();
            default -> throw new IllegalStateException("Unknown AI key format");
        };
    }

    ///

    public MouthInterface getMouthImpl() {
        String apiKey = SystemSession.getInstance().getTtsApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "TTS");
        switch (provider) {
            case GOOGLE_TTS:
                return GoogleTTSImpl.getInstance();
            // TODO: Add ElevenLabs, AWS Polly, etc.
            default:
                EventBusManager.publish(new AppLogEvent("Unknown TTS key format"));
                EventBusManager.publish(new AiVoxResponseEvent("Using default Google TTS—confirm?"));
                return PiperTTS.getInstance();
        }
    }

    ///

    public EarsInterface getEarsImpl() {
        String apiKey =  SystemSession.getInstance().getSttApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "STT"); // Fixed category
        switch (provider) {
            case GOOGLE_STT:
                return new GoogleSTTImpl();
            // TODO: Add Deepgram, Azure STT, etc.
            default:
                EventBusManager.publish(new AppLogEvent("Unknown STT key format"));
                EventBusManager.publish(new AiVoxResponseEvent("Using default Google STT—confirm?"));
                return new GoogleSTTImpl();
        }
    }
}
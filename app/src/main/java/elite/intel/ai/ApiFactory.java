package elite.intel.ai;

import elite.intel.ai.brain.*;
import elite.intel.ai.brain.commons.PromptFactory;
import elite.intel.ai.brain.commons.ResponseRouter;
import elite.intel.ai.brain.ollama.OllamaAnalysisEndpoint;
import elite.intel.ai.brain.ollama.OllamaCommandEndPoint;
import elite.intel.ai.brain.ollama.OllamaUserInputProcessor;
import elite.intel.ai.brain.openai.OpenAiAnalysisEndPoint;
import elite.intel.ai.brain.openai.OpenAiChatEndPoint;
import elite.intel.ai.brain.openai.OpenAiCommandEndPoint;
import elite.intel.ai.brain.xai.GrokAnalysisEndpoint;
import elite.intel.ai.brain.xai.GrokChatEndPoint;
import elite.intel.ai.brain.xai.GrokCommandEndPoint;
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
    private final SystemSession systemSession;

    private ApiFactory() {
        // Prevent instantiation.
        this.systemSession = SystemSession.getInstance();
    }

    public static synchronized ApiFactory getInstance() {
        if (instance == null) {
            instance = new ApiFactory();
        }
        return instance;
    }

    public AiAnalysisInterface getAnalysisEndpoint() {
        if(systemSession.useLocalQueryLlm()){
            return OllamaAnalysisEndpoint.getInstance();
        }
        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokAnalysisEndpoint.getInstance();
            case OPENAI -> OpenAiAnalysisEndPoint.getInstance();
            default -> OllamaAnalysisEndpoint.getInstance();
        };

    }

    public AIChatInterface getChatEndpoint() {

        if(systemSession.useLocalQueryLlm()){
            return OllamaCommandEndPoint.getInstance();
        }

        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokChatEndPoint.getInstance();
            case OPENAI -> OpenAiChatEndPoint.getInstance();
            default -> OllamaCommandEndPoint.getInstance();
        };
    }

    public AiPromptFactory getAiPromptFactory() {
        return PromptFactory.getInstance();

        ///NOTE: there is the same prompt for all supported LLMs at the moment.
        ///NOTE: if that changes use the code below

/*        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            // testing ollama prompts with cloud llms. If good, that will be the default
            case GROK -> PromptFactory.getInstance();
            case OPENAI -> PromptFactory.getInstance();
            default -> PromptFactory.getInstance();
        };*/
    }

    public AiCommandInterface getCommandEndpoint() {
        if(systemSession.useLocalCommandLlm()){
            return OllamaUserInputProcessor.getInstance();
        }

        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokCommandEndPoint.getInstance();
            case OPENAI -> OpenAiCommandEndPoint.getInstance();
            default -> OllamaUserInputProcessor.getInstance();
        };
    }

    public AIRouterInterface getAiRouter() {
        return ResponseRouter.getInstance();
    }

    ///
    public MouthInterface getMouthImpl() {
        if(systemSession.useLocalTTS()){
            return PiperTTS.getInstance();
        }

        String apiKey = SystemSession.getInstance().getTtsApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "TTS");
        switch (provider) {
            case GOOGLE_TTS:
                return GoogleTTSImpl.getInstance();
            // TODO: Add ElevenLabs, AWS Polly, etc.
            default:
                return PiperTTS.getInstance();
        }
    }

    ///
    public EarsInterface getEarsImpl() {
        String apiKey = SystemSession.getInstance().getSttApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "STT"); // Fixed category
        switch (provider) {
            case GOOGLE_STT:
                return new GoogleSTTImpl();
            // TODO: Add Deepgram, Azure STT, etc.
            default:
                EventBusManager.publish(new AppLogEvent("Unknown STT key format"));
                EventBusManager.publish(new AiVoxResponseEvent("Google Speech To Text Key is required."));
                return new GoogleSTTImpl();
        }
    }
}
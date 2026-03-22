package elite.intel.ai;

import elite.intel.ai.brain.*;
import elite.intel.ai.brain.anthropic.AnthropicAnalysisEndpoint;
import elite.intel.ai.brain.anthropic.AnthropicCommandEndPoint;
import elite.intel.ai.brain.anthropic.AnthropicUserEndPoint;
import elite.intel.ai.brain.commons.PromptFactory;
import elite.intel.ai.brain.commons.ResponseRouter;
import elite.intel.ai.brain.gemini.GeminiAnalysisEndpoint;
import elite.intel.ai.brain.gemini.GeminiChatEndPoint;
import elite.intel.ai.brain.gemini.GeminiCommandEndPoint;
import elite.intel.ai.brain.lmstudio.LMStudioAnalysisEndpoint;
import elite.intel.ai.brain.lmstudio.LMStudioCommandEndPoint;
import elite.intel.ai.brain.lmstudio.LMStudioUserInputProcessor;
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
import elite.intel.ai.ears.whisper.WhisperSTTImpl;
import elite.intel.ai.mouth.MouthInterface;
import elite.intel.ai.mouth.google.GoogleTTSImpl;
import elite.intel.ai.mouth.kokoro.KokoroTTS;
import elite.intel.session.SystemSession;

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
        if (systemSession.useLocalQueryLlm()) {
            return switch (systemSession.getLocalLlmProvider()) {
                case LMSTUDIO -> LMStudioAnalysisEndpoint.getInstance();
                default -> OllamaAnalysisEndpoint.getInstance();
            };
        }
        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokAnalysisEndpoint.getInstance();
            case OPENAI -> OpenAiAnalysisEndPoint.getInstance();
            case ANTHROPIC -> AnthropicAnalysisEndpoint.getInstance();
            case GOOGLE_LLM -> GeminiAnalysisEndpoint.getInstance();
            default -> OllamaAnalysisEndpoint.getInstance();
        };

    }

    public AIChatInterface getChatEndpoint() {

        if (systemSession.useLocalQueryLlm()) {
            return switch (systemSession.getLocalLlmProvider()) {
                case LMSTUDIO -> LMStudioCommandEndPoint.getInstance();
                default -> OllamaCommandEndPoint.getInstance();
            };
        }

        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokChatEndPoint.getInstance();
            case OPENAI -> OpenAiChatEndPoint.getInstance();
            case ANTHROPIC -> AnthropicUserEndPoint.getInstance();
            case GOOGLE_LLM -> GeminiChatEndPoint.getInstance();
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
        if (systemSession.useLocalCommandLlm()) {
            return switch (systemSession.getLocalLlmProvider()) {
                case LMSTUDIO -> LMStudioUserInputProcessor.getInstance();
                default -> OllamaUserInputProcessor.getInstance();
            };
        }

        String apiKey = SystemSession.getInstance().getAiApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "LLM");
        return switch (provider) {
            case GROK -> GrokCommandEndPoint.getInstance();
            case OPENAI -> OpenAiCommandEndPoint.getInstance();
            case ANTHROPIC -> AnthropicCommandEndPoint.getInstance();
            case GOOGLE_LLM -> GeminiCommandEndPoint.getInstance();
            default -> OllamaUserInputProcessor.getInstance();
        };
    }

    public AIRouterInterface getAiRouter() {
        return ResponseRouter.getInstance();
    }

    ///
    @SuppressWarnings({"SwitchStatementWithTooFewBranches", "EnhancedSwitchMigration"})
    public MouthInterface getMouthImpl() {
        if (systemSession.useLocalTTS()) {
            return KokoroTTS.getInstance();
        }

        String apiKey = SystemSession.getInstance().getTtsApiKey();
        ProviderEnum provider = KeyDetector.detectProvider(apiKey, "TTS");
        switch (provider) {
            case GOOGLE_TTS:
                return GoogleTTSImpl.getInstance();
            // TODO: Add ElevenLabs, AWS Polly, etc.
            default:
                return KokoroTTS.getInstance();
        }
    }

    /// -- no choises here
    public EarsInterface getEarsImpl() {
        return new WhisperSTTImpl();
    }
}
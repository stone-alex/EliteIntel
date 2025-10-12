package elite.intel.ai.brain.commons;

import elite.intel.ai.ApiFactory;
import elite.intel.ai.brain.AIChatInterface;
import elite.intel.ai.brain.AIRouterInterface;
import elite.intel.ai.brain.AiPromptFactory;

public abstract class CommandEndPoint extends AiEndPoint {

    private final AiPromptFactory contextFactory;
    private final AIRouterInterface router;
    private final AIChatInterface chatInterface;


    protected CommandEndPoint() {
        this.router = ApiFactory.getInstance().getAiRouter();
        this.chatInterface = ApiFactory.getInstance().getChatEndpoint();
        this.contextFactory = ApiFactory.getInstance().getAiPromptFactory();
    }

    public AiPromptFactory getContextFactory() {
        return contextFactory;
    }

    public AIRouterInterface getRouter() {
        return router;
    }

    public AIChatInterface getChatInterface() {
        return chatInterface;
    }

    protected String buildVoiceRequest(String transcribedText) {
        return contextFactory.generatePlayerInstructions(String.valueOf(transcribedText));
    }

    protected String buildSystemRequest(String systemInput) {
        return contextFactory.generateSystemInstructions(systemInput);
    }
}

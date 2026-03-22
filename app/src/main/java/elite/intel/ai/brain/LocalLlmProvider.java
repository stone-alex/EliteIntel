package elite.intel.ai.brain;

public enum LocalLlmProvider {
    OLLAMA("http://localhost:11434/api/chat"),
    LMSTUDIO("http://localhost:1234/v1/chat/completions");

    private final String defaultUrl;

    LocalLlmProvider(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }
}

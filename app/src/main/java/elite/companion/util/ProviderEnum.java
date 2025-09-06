package elite.companion.util;

public enum ProviderEnum {
    GROK("LLM"),
    GOOGLE_STT("STT"),
    GOOGLE_TTS("TTS"),
    GOOGLE_LLM("LLM"),
    OPENAI("LLM"),
    ANTHROPIC("LLM"),
    AWS_LLM("LLM"),
    AWS_STT("STT"),
    AWS_TTS("TTS"),
    AZURE_LLM("LLM"),
    AZURE_STT("STT"),
    AZURE_TTS("TTS"),
    ELEVEN_LABS("TTS"),
    DEEP_GRAM("STT"),
    IBM_WATSON_STT("STT"),
    IBM_WATSON_TTS("TTS"),
    UNKNOWN("UNKNOWN");

    private final String category;

    public boolean supportsCategory(String category) {
        return this.category.equals(category);
    }

    ProviderEnum(String category) {
        this.category = category;
    }
}
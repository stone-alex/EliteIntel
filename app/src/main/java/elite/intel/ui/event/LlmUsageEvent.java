package elite.intel.ui.event;

public record LlmUsageEvent(
        String provider,
        String model,
        int promptTokens,
        int completionTokens,
        int cachedTokens,
        int cacheWrittenTokens,
        double tps
) {
}

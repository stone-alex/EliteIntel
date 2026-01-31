package elite.intel.util.json;

import com.google.gson.annotations.SerializedName;

public record OllamaMetadata(
        String model,
        @SerializedName("prompt_eval_count") int promptTokens,
        @SerializedName("eval_count") int completionTokens,
        @SerializedName("total_duration") long totalDurationNs,
        @SerializedName("prompt_eval_duration") long promptEvalDurationNs,
        @SerializedName("eval_duration") long evalDurationNs
) {
    public int totalTokens() {
        return promptTokens + completionTokens;
    }

    public double tokensPerSecond() {
        if (totalDurationNs == 0) return 0.0;
        return (double) totalTokens() * 1_000_000_000 / totalDurationNs;
    }

    @Override
    public String toString() {
        return model +
                " | Prompt Tokens: " + promptTokens +
                " | Completion: " + completionTokens +
                " | Total: " + totalTokens() +
                " | Speed≈" + String.format("%.1f", tokensPerSecond()) + " t/s";
    }
}
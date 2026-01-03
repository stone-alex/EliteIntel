package elite.intel.util.json;

import com.google.gson.annotations.SerializedName;

import java.util.StringJoiner;

public record LlmMetadata(
        String model,
        @SerializedName("usage") Usage usage
) {
    public record Usage(
            @SerializedName("prompt_tokens") int promptTokens,
            @SerializedName("completion_tokens") int completionTokens,
            @SerializedName("total_tokens") int totalTokens,
            @SerializedName("prompt_tokens_details") TokenDetails promptDetails,
            @SerializedName("completion_tokens_details") TokenDetails completionDetails
    ) {
        public record TokenDetails(
                @SerializedName("text_tokens") int textTokens,
                @SerializedName("audio_tokens") int audioTokens,
                @SerializedName("image_tokens") int imageTokens,
                @SerializedName("cached_tokens") int cachedTokens,
                @SerializedName("reasoning_tokens") int reasoningTokens,
                @SerializedName("accepted_prediction_tokens") int acceptedPredictionTokens,
                @SerializedName("rejected_prediction_tokens") int rejectedPredictionTokens
        ) {
            public int cachedTokens() { return cachedTokens; }
            public int reasoningTokens() { return reasoningTokens; }
        }

        public int cachedTokens() { return promptDetails != null ? promptDetails.cachedTokens() : 0; }
        public int totalTokens() { return totalTokens; }
        public int reasoningTokens() { return completionDetails != null ? completionDetails.reasoningTokens() : 0; }
    }

    public String model() { return model; }
    public int cachedTokens() { return usage == null ? 0 :  usage.cachedTokens(); }
    public int totalTokens() { return  usage == null ? 0 :  usage.totalTokens(); }
    public int reasoningTokens() { return  usage == null ? 0 :  usage.reasoningTokens(); }

    @Override public String toString() {
        return new StringJoiner(", ", LlmMetadata.class.getSimpleName() + "[", "]")
                .add("model='" + model + "'")
                .add("usage=" + usage)
                .toString();
    }
}
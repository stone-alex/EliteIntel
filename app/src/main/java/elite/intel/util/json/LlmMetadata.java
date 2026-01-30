package elite.intel.util.json;

import com.google.gson.annotations.SerializedName;

import java.util.StringJoiner;

public record LlmMetadata (
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
        @Override public String toString() {
            return " |Prompt Tokens: " + promptTokens +
                    " | Completion: " + completionTokens +
                    " | Cached: " + promptDetails.cachedTokens +
                    " | Total: " + totalTokens;
        }

        public record TokenDetails(
                @SerializedName("text_tokens") int textTokens,
                @SerializedName("cached_tokens") int cachedTokens,
                @SerializedName("reasoning_tokens") int reasoningTokens,
                @SerializedName("accepted_prediction_tokens") int acceptedPredictionTokens,
                @SerializedName("rejected_prediction_tokens") int rejectedPredictionTokens
        ) {

        }
    }

    public String model() { return model; }

    @Override public String toString() {
        return model +" >" + usage;
    }
}
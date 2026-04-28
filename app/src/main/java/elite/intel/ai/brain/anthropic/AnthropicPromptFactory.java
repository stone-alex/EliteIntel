package elite.intel.ai.brain.anthropic;

import elite.intel.ai.brain.Reducer;
import elite.intel.ai.brain.SttCorrector;
import elite.intel.ai.brain.commons.PromptFactory;

import java.util.Map;

public class AnthropicPromptFactory extends PromptFactory {

    private static final AnthropicPromptFactory INSTANCE = new AnthropicPromptFactory();

    private AnthropicPromptFactory() {
    }

    public static AnthropicPromptFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Stable (per-session) block sent as the cached system prefix.
     * Contains all command rules + the full action map to guarantee the 2048-token
     * cache-write threshold is met on every Anthropic model.
     */
    public String generateFullUserInputPromptForAnthropicCaching() {
        Map<String, String> fullMap = actionsMap.actionMap(isDryRun);
        sttVocabulary = SttCorrector.extractVocabulary(fullMap);
        StringBuilder sb = new StringBuilder();
        buildCommandRules(sb);
        sb.append("\nAVAILABLE ACTIONS:\n");
        sb.append(Reducer.formatActions(fullMap));
        return sb.toString();
    }

    /**
     * Dynamic (per-request) block sent as the uncached system suffix.
     * Contains only the Reducer-filtered subset relevant to the current input,
     * guiding routing accuracy without polluting the stable cached prefix.
     */
    public String generateUserInputActionListForAnthropicPrompt(String rawUserInput) {
        return "ACTIONS MOST RELEVANT TO THIS INPUT:\n" + Reducer.formatActions(reduce(rawUserInput));
    }
}

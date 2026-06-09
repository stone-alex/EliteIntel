package elite.intel.ai.brain.actions.customcommand;

import elite.intel.ai.brain.actions.handlers.commands.CommandHandler;
import elite.intel.ai.brain.InputNormalizer;
import elite.intel.ai.brain.i18n.AiActionLocalizations;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.CustomCommandsSummaryChangedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Singleton coordinator for user-defined customCommands.
 * <p>
 * Call {@link #load()} once at application startup before {@code ResponseRouter} is initialized.
 * After that the registry is read-only and thread-safe without locking.
 */
public final class CustomCommandRegistry {

    private static final Logger log = LogManager.getLogger(CustomCommandRegistry.class);
    private static final CustomCommandRegistry INSTANCE = new CustomCommandRegistry();

    /** Written once during {@link #load()}, read-only after. */
    private volatile List<CustomCommandDefinition> customCommands = Collections.emptyList();
    private final CustomCommandRepository repository = new CustomCommandRepository();

    private CustomCommandRegistry() {}

    public static CustomCommandRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Loads customCommands from {@code custom_commands.json}. Must be called before
     * {@code ResponseRouter} (and therefore {@code CommandHandlerFactory}) is first used.
     */
    public void load() {
        customCommands = repository.load();
        log.info("Custom command registry: {} command(s) loaded, {} skipped",
                customCommands.size(), repository.getLastSkippedCount());
    }

    /** Returns the loaded custom command list as an immutable snapshot. Empty until {@link #load()} is called. */
    public List<CustomCommandDefinition> getCustomCommands() {
        return List.copyOf(customCommands);
    }

    /** Returns the number of customCommands skipped during the most recent {@link #load()} due to validation failures. */
    public int getSkippedOnLastLoad() {
        return repository.getLastSkippedCount();
    }

    /** Returns human-readable labels for customCommands skipped during the most recent {@link #load()}. */
    public List<String> getSkippedLabelsOnLastLoad() {
        return repository.getLastSkippedLabels();
    }

    /** Returns {@code true} if the most recent {@link #load()} restored customCommands from the backup file. */
    public boolean wasLastLoadRestoredFromBackup() {
        return repository.wasRestoredFromBackup();
    }

    /**
     * Replaces the in-memory customCommand registry with a validated immutable snapshot.
     * Persistence is owned by {@link CustomCommandRepository}; this method only updates runtime state.
     */
    public void replaceCustomCommands(List<CustomCommandDefinition> customCommands) {
        setCustomCommands(customCommands);
        log.info("Custom command registry: {} command(s) active after replace", this.customCommands.size());
        EventBusManager.publish(new CustomCommandsSummaryChangedEvent(this.customCommands.size()));
    }

    /**
     * Adds custom command phrase->id entries to the LLM action map.
     * Called from {@code AiActionsMap.actionMap()} so custom command trigger phrases reach the Reducer
     * and appear in the ACTIONS block sent to the LLM.
     */
    public void contributeToActionMap(Map<String, String> map) {
        Set<String> protectedPhrases = new HashSet<>();
        for (String phrase : map.keySet()) {
            protectedPhrases.add(normalizePhrase(phrase));
        }
        for (CustomCommandDefinition customCommand : customCommands) {
            for (String phrase : AiActionLocalizations.splitPhraseGroup(customCommand.getPhrases())) {
                String normalizedPhrase = normalizePhrase(phrase);
                if (protectedPhrases.contains(normalizedPhrase)) {
                    // Graceful degradation: skip conflicting phrase rather than blocking the entire customCommand.
                    log.warn("CustomCommand phrase [{}] conflicts with an existing action map entry - phrase skipped", phrase);
                    continue;
                }
                map.put(normalizedPhrase, customCommand.getActionKey());
                protectedPhrases.add(normalizedPhrase);
                log.debug("CustomCommand phrase registered: [{}] -> {}", normalizedPhrase, customCommand.getActionKey());
            }
        }
    }

    private static String normalizePhrase(String phrase) {
        String normalized = InputNormalizer.getInstance().normalize(phrase == null ? "" : phrase);
        return normalized == null ? "" : normalized.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Package-private test seam - injects a known custom command list without file I/O.
     * Resets the registry to the provided list; {@link #load()} is not called.
     */
    void setCustomCommands(List<CustomCommandDefinition> customCommands) {
        this.customCommands = customCommands == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(customCommands));
    }

    /**
     * Appends a {@code CUSTOM COMMAND PARAMS} section to {@code sb} for any parameterized customCommands
     * whose IDs appear as values in the already-reduced action map.
     * Called from {@code PromptFactory.generateUserInputSystemPrompt()} after
     * {@code Reducer.formatActions()} so the LLM sees parameter rules only for customCommands
     * that survived reduction — avoids sending param rules for unrelated customCommands and reduces token usage.
     */
    public void appendCustomCommandParamRules(Map<String, String> reducedActions, StringBuilder sb) {
        Set<String> activeIds = new HashSet<>(reducedActions.values());
        List<CustomCommandDefinition> activeCustomCommands = customCommands.stream()
                .filter(m -> activeIds.contains(m.getActionKey()))
                .filter(m -> !m.getParameters().isEmpty())
                .toList();
        if (activeCustomCommands.isEmpty()) return;

        sb.append("\nCUSTOM COMMAND PARAMS (required for custom command actions above — include ALL required params):\n\n");
        for (CustomCommandDefinition customCommand : activeCustomCommands) {
            sb.append("  ").append(customCommand.getActionKey()).append(":\n");
            for (CustomCommandParameterSpec param : customCommand.getParameters()) {
                sb.append("    ").append(param.getName())
                  .append(" (").append(param.getType());
                if (param.isRequired()) sb.append(", required");
                sb.append(")");
                if (!param.getDescription().isBlank()) {
                    sb.append(" — ").append(param.getDescription());
                }
                List<String> examples = param.getExamples();
                if (!examples.isEmpty()) {
                    sb.append(". E.g.: ").append(String.join(", ", examples));
                }
                sb.append("\n");
                if (param.getExtractionHint() != null && !param.getExtractionHint().isBlank()) {
                    sb.append("      Hint: ").append(param.getExtractionHint()).append("\n");
                }
            }
        }
    }

    /**
     * Registers a {@link CustomCommandHandler} per custom command into the command handler map.
     * Called from {@code CommandHandlerFactory.registerCommandHandlers()} so that
     * {@code ResponseRouter} routes user-defined action IDs through the normal command dispatch path.
     */
    public void contributeToHandlerMap(Map<String, CommandHandler> map) {
        Set<String> protectedActionIds = new HashSet<>(map.keySet());
        for (CustomCommandDefinition customCommand : customCommands) {
            if (protectedActionIds.contains(customCommand.getActionKey())) {
                log.warn("Custom command actionKey '{}' conflicts with an existing command handler - custom command skipped",
                        customCommand.getActionKey());
                continue;
            }
            map.put(customCommand.getActionKey(), new CustomCommandHandler(customCommand));
            log.debug("Custom command handler registered: {}", customCommand.getActionKey());
        }
    }
}

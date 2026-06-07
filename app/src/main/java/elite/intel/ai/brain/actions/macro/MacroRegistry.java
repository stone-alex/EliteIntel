package elite.intel.ai.brain.actions.macro;

import elite.intel.ai.brain.actions.handlers.commands.CommandHandler;
import elite.intel.ai.brain.InputNormalizer;
import elite.intel.ai.brain.i18n.AiActionLocalizations;
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
 * Singleton coordinator for user-defined macros.
 * <p>
 * Call {@link #load()} once at application startup before {@code ResponseRouter} is initialized.
 * After that the registry is read-only and thread-safe without locking.
 */
public final class MacroRegistry {

    private static final Logger log = LogManager.getLogger(MacroRegistry.class);
    private static final MacroRegistry INSTANCE = new MacroRegistry();

    /** Written once during {@link #load()}, read-only after. */
    private volatile List<MacroDefinition> macros = Collections.emptyList();
    private final MacroRepository repository = new MacroRepository();

    private MacroRegistry() {}

    public static MacroRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Loads custom commands from {@code custom_commands.json}. Must be called before
     * {@code ResponseRouter} (and therefore {@code CommandHandlerFactory}) is first used.
     */
    public void load() {
        macros = repository.load();
        log.info("Custom command registry: {} command(s) loaded, {} skipped",
                macros.size(), repository.getLastSkippedCount());
    }

    /** Returns the loaded macro list as an immutable snapshot. Empty until {@link #load()} is called. */
    public List<MacroDefinition> getMacros() {
        return List.copyOf(macros);
    }

    /** Returns the number of macros skipped during the most recent {@link #load()} due to validation failures. */
    public int getSkippedOnLastLoad() {
        return repository.getLastSkippedCount();
    }

    /** Returns human-readable labels for macros skipped during the most recent {@link #load()}. */
    public List<String> getSkippedLabelsOnLastLoad() {
        return repository.getLastSkippedLabels();
    }

    /** Returns {@code true} if the most recent {@link #load()} restored macros from the backup file. */
    public boolean wasLastLoadRestoredFromBackup() {
        return repository.wasRestoredFromBackup();
    }

    /**
     * Replaces the in-memory macro registry with a validated immutable snapshot.
     * Persistence is owned by {@link MacroRepository}; this method only updates runtime state.
     */
    public void replaceMacros(List<MacroDefinition> macros) {
        setMacros(macros);
        log.info("Custom command registry: {} command(s) active after replace", this.macros.size());
    }

    /**
     * Adds macro phrase->id entries to the LLM action map.
     * Called from {@code AiActionsMap.actionMap()} so macro trigger phrases reach the Reducer
     * and appear in the ACTIONS block sent to the LLM.
     */
    public void contributeToActionMap(Map<String, String> map) {
        Set<String> protectedPhrases = new HashSet<>();
        for (String phrase : map.keySet()) {
            protectedPhrases.add(normalizePhrase(phrase));
        }
        for (MacroDefinition macro : macros) {
            for (String phrase : AiActionLocalizations.splitPhraseGroup(macro.getPhrases())) {
                String normalizedPhrase = normalizePhrase(phrase);
                if (protectedPhrases.contains(normalizedPhrase)) {
                    // Graceful degradation: skip conflicting phrase rather than blocking the entire macro.
                    log.warn("Macro phrase [{}] conflicts with an existing action map entry - phrase skipped", phrase);
                    continue;
                }
                map.put(normalizedPhrase, macro.getActionKey());
                protectedPhrases.add(normalizedPhrase);
                log.debug("Macro phrase registered: [{}] -> {}", normalizedPhrase, macro.getActionKey());
            }
        }
    }

    private static String normalizePhrase(String phrase) {
        String normalized = InputNormalizer.getInstance().normalize(phrase == null ? "" : phrase);
        return normalized == null ? "" : normalized.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Package-private test seam - injects a known macro list without file I/O.
     * Resets the registry to the provided list; {@link #load()} is not called.
     */
    void setMacros(List<MacroDefinition> macros) {
        this.macros = macros == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(macros));
    }

    /**
     * Appends a {@code MACRO PARAMS} section to {@code sb} for any parameterized macros
     * whose IDs appear as values in the already-reduced action map.
     * Called from {@code PromptFactory.generateUserInputSystemPrompt()} after
     * {@code Reducer.formatActions()} so the LLM sees parameter rules only for macros
     * that survived reduction — avoids sending param rules for unrelated macros and reduces token usage.
     */
    public void appendMacroParamRules(Map<String, String> reducedActions, StringBuilder sb) {
        Set<String> activeIds = new HashSet<>(reducedActions.values());
        List<MacroDefinition> activeMacros = macros.stream()
                .filter(m -> activeIds.contains(m.getActionKey()))
                .filter(m -> !m.getParameters().isEmpty())
                .toList();
        if (activeMacros.isEmpty()) return;

        sb.append("\nMACRO PARAMS (required for macro actions above — include ALL required params):\n\n");
        for (MacroDefinition macro : activeMacros) {
            sb.append("  ").append(macro.getActionKey()).append(":\n");
            for (MacroParameterSpec param : macro.getParameters()) {
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
        for (MacroDefinition macro : macros) {
            if (protectedActionIds.contains(macro.getActionKey())) {
                log.warn("Custom command actionKey '{}' conflicts with an existing command handler - custom command skipped",
                        macro.getActionKey());
                continue;
            }
            map.put(macro.getActionKey(), new CustomCommandHandler(macro));
            log.debug("Custom command handler registered: {}", macro.getActionKey());
        }
    }
}

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

    private MacroRegistry() {}

    public static MacroRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Loads macros from {@code macros.json}. Must be called before
     * {@code ResponseRouter} (and therefore {@code CommandHandlerFactory}) is first used.
     */
    public void load() {
        macros = new MacroRepository().load();
        log.info("MacroRegistry: {} macro(s) loaded", macros.size());
    }

    /** Returns the loaded macro list as an immutable snapshot. Empty until {@link #load()} is called. */
    public List<MacroDefinition> getMacros() {
        return List.copyOf(macros);
    }

    /**
     * Replaces the in-memory macro registry with a validated immutable snapshot.
     * Persistence is owned by {@link MacroRepository}; this method only updates runtime state.
     */
    public void replaceMacros(List<MacroDefinition> macros) {
        setMacros(macros);
        log.info("MacroRegistry: {} macro(s) active after replace", this.macros.size());
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
                    log.warn("Macro phrase [{}] conflicts with an existing action map entry - phrase skipped", phrase);
                    continue;
                }
                map.put(normalizedPhrase, macro.getId());
                protectedPhrases.add(normalizedPhrase);
                log.debug("Macro phrase registered: [{}] -> {}", normalizedPhrase, macro.getId());
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
     * Registers a {@link MacroCommandHandler} per macro into the command handler map.
     * Called from {@code CommandHandlerFactory.registerCommandHandlers()} so that
     * {@code ResponseRouter} routes macro action IDs through the normal command dispatch path.
     */
    public void contributeToHandlerMap(Map<String, CommandHandler> map) {
        Set<String> protectedActionIds = new HashSet<>(map.keySet());
        for (MacroDefinition macro : macros) {
            if (protectedActionIds.contains(macro.getId())) {
                log.warn("Macro id '{}' conflicts with an existing command handler - macro skipped",
                        macro.getId());
                continue;
            }
            map.put(macro.getId(), new MacroCommandHandler(macro));
            log.debug("Macro handler registered: {}", macro.getId());
        }
    }
}

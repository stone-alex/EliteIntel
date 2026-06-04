package elite.intel.ai.brain.actions.macro;

import elite.intel.ai.brain.actions.handlers.commands.CommandHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

    /** Returns the loaded macro list. Unmodifiable; empty until {@link #load()} is called. */
    public List<MacroDefinition> getMacros() {
        return macros;
    }

    /**
     * Adds macro phrase->id entries to the LLM action map.
     * Called from {@code AiActionsMap.actionMap()} so macro trigger phrases reach the Reducer
     * and appear in the ACTIONS block sent to the LLM.
     */
    public void contributeToActionMap(Map<String, String> map) {
        Set<String> protectedPhrases = new HashSet<>(map.keySet());
        for (MacroDefinition macro : macros) {
            if (protectedPhrases.contains(macro.getPhrases())) {
                log.warn("Macro phrase [{}] conflicts with an existing action map entry - macro skipped",
                        macro.getPhrases());
                continue;
            }
            map.put(macro.getPhrases(), macro.getId());
            log.debug("Macro phrase registered: [{}] -> {}", macro.getPhrases(), macro.getId());
        }
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

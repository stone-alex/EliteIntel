package elite.intel.ai.brain.i18n;

import elite.intel.session.Status;

import java.util.Map;
import java.util.Set;

public interface AiActionAliasProvider {

    void addAliases(Map<String, String> map, Status status, boolean isDryRun);

    /**
     * Phrases that allow a transcript to pass the sleep-mode STT gate.
     */
    Set<String> wakeBypassPhrases();

    /**
     * Listen-type prefixes that can be stripped before forwarding to the AI.
     * E.g. "listen open galaxy map" → "open galaxy map".
     * Pure wake phrases (no content follows) are NOT in this set.
     */
    Set<String> listenBypassPrefixes();
}
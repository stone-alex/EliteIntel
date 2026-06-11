package elite.intel.ai.brain.i18n;

import java.util.LinkedHashMap;

/**
 * French synonym substitution rules for the InputNormalizer.
 * <p>
 * French uses liaison and elision (e.g. "l'", "d'") — plain substring replacement
 * can match across word boundaries unexpectedly. Keep entries to complete,
 * unambiguous phrases. Prefer adding variants to {@link FrenchAiActionAliases}.
 */
public class FrenchInputNormalizerRules implements InputNormalizerProvider {

    @Override
    public LinkedHashMap<String, String> buildSynonymMap() {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        // Add French synonym rules here as they are identified during testing.
        return m;
    }
}

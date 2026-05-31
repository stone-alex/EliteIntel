package elite.intel.ai.brain.i18n;

import elite.intel.session.Status;
import elite.intel.session.SystemSession;

import java.util.Map;
import java.util.Set;

public final class AiActionLocalizations {

    private AiActionLocalizations() {
    }

    private static AiActionAliasProvider provider() {
        return switch (SystemSession.getInstance().getLanguage()) {
            case EN -> new EnglishAiActionAliases();
            case RU -> new RussianAiActionAliases();
            case UK -> new UkrainianAiActionAliases();
            case DE -> new GermanAiActionAliases();
            case FR -> new FrenchAiActionAliases();
        };
    }

    public static void addAliases(Map<String, String> map, Status status, boolean isDryRun) {
        provider().addAliases(map, status, isDryRun);
    }

    public static Set<String> wakeBypassPhrases() {
        return provider().wakeBypassPhrases();
    }

    public static Set<String> listenBypassPrefixes() {
        return provider().listenBypassPrefixes();
    }
}

package elite.intel.ai.brain.i18n;

import elite.intel.session.Status;

import java.util.Map;

public final class AiActionLocalizations {

    private static final AiActionLanguage ACTIVE_LANGUAGE = AiActionLanguage.DE;

    private AiActionLocalizations() {
    }

    public static void addAliases(Map<String, String> map, Status status, boolean isDryRun) {
        AiActionAliasProvider provider = switch (ACTIVE_LANGUAGE) {
            case EN -> new EnglishAiActionAliases();
            case RU -> new RussianAiActionAliases();
            case UK -> new UkrainianAiActionAliases();
            case DE -> new GermanAiActionAliases();

        };

        provider.addAliases(map, status, isDryRun);
    }
}
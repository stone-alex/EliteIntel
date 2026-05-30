package elite.intel.ai.brain.i18n;

import elite.intel.session.Status;
import elite.intel.session.SystemSession;

import java.util.Map;

public final class AiActionLocalizations {

    private AiActionLocalizations() {
    }

    public static void addAliases(Map<String, String> map, Status status, boolean isDryRun) {
        elite.intel.i18n.Language language = SystemSession.getInstance().getLanguage();
        AiActionAliasProvider provider = switch (language) {
            case EN -> new EnglishAiActionAliases();
            case RU -> new RussianAiActionAliases();
            case UK -> new UkrainianAiActionAliases();
            case DE -> new GermanAiActionAliases();

        };

        provider.addAliases(map, status, isDryRun);
    }
}

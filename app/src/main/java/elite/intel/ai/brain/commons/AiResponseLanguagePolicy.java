package elite.intel.ai.brain.commons;

import elite.intel.ai.KeyDetector;
import elite.intel.ai.ProviderEnum;
import elite.intel.i18n.Language;
import elite.intel.session.SystemSession;

public final class AiResponseLanguagePolicy {

    private AiResponseLanguagePolicy() {
    }

    public static Language resolveEffectiveAiResponseLanguage(SystemSession systemSession) {
        return isGoogleTtsConfiguredAndUsable(systemSession)
                ? systemSession.getLanguage()
                : Language.EN;
    }

    public static boolean isGoogleTtsConfiguredAndUsable(SystemSession systemSession) {
        if (systemSession.useLocalTTS()) {
            return false;
        }
        return KeyDetector.detectProvider(systemSession.getTtsApiKey(), "TTS") == ProviderEnum.GOOGLE_TTS;
    }
}

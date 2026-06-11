package elite.intel.ai.brain.i18n.uk;

import elite.intel.ai.brain.i18n.PromptLanguageRules;

public class UkrainianPromptRules implements PromptLanguageRules {

    @Override
    public String languageName() {
        return "Ukrainian";
    }

    @Override
    public String queryStarterExamples() {
        return "що, де, як, який, яка, які, чому, чи є, скільки, на якій, в якій, розкажи";
    }

    @Override
    public String commandVerbExamples() {
        return "покажи / відкрий / знайди / шукай / активуй / вимкни / проклади маршрут / випусти / прибери / увімкни / вимкни";
    }

    @Override
    public String queryPhraseExamples() {
        return "де / що / скільки / чи є / який / яка / які / на якій станції / в якій системі";
    }
}

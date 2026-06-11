package elite.intel.ai.brain.i18n.ru;

import elite.intel.ai.brain.i18n.PromptLanguageRules;

public class RussianPromptRules implements PromptLanguageRules {

    @Override
    public String languageName() {
        return "Russian";
    }

    @Override
    public String queryStarterExamples() {
        return "что, где, как, какой, какая, какие, почему, есть ли, сколько, на какой, в какой, расскажи";
    }

    @Override
    public String commandVerbExamples() {
        return "покажи / открой / найди / ищи / активируй / отключи / проложи маршрут / выпусти / убери / включи / выключи";
    }

    @Override
    public String queryPhraseExamples() {
        return "где / что / сколько / есть ли / какой / какая / какие / на какой станции / в какой системе";
    }
}

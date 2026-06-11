package elite.intel.ai.brain.i18n.fr;

import elite.intel.ai.brain.i18n.PromptLanguageRules;

public class FrenchPromptRules implements PromptLanguageRules {

    @Override
    public String languageName() {
        return "French";
    }

    @Override
    public String queryStarterExamples() {
        return "quoi, où, comment, quel, quelle, quels, pourquoi, y a-t-il, combien, dis-moi";
    }

    @Override
    public String commandVerbExamples() {
        return "montre / ouvre / trouve / cherche / active / désactive / navigue / trace / déploie / rentre / allume / éteins";
    }

    @Override
    public String queryPhraseExamples() {
        return "où / quoi / combien / y a-t-il / quel / quelle / quelle station / quel système";
    }

    @Override
    public String disambiguationHints() {
        // TODO: if LLM constantly confuses term A with term B add a disambiguation rule here.
        return PromptLanguageRules.super.disambiguationHints();
    }
}

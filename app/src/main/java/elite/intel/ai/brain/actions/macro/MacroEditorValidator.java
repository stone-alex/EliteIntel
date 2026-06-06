package elite.intel.ai.brain.actions.macro;

import elite.intel.ai.brain.actions.Commands;
import elite.intel.ai.brain.i18n.AiActionLocalizations;
import elite.intel.session.Status;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates editor-created macros before they are persisted or registered for command dispatch.
 */
public final class MacroEditorValidator {

    // Allows dot, colon, and dash so macro IDs can mirror the action ID conventions used by Commands enum.
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9_.:-]+");
    // Stricter than SAFE_ID: param names must be valid identifier fragments usable in ${…} templates.
    private static final Pattern VALID_PARAM_NAME = Pattern.compile("[A-Za-z0-9_]+");

    private MacroEditorValidator() {
    }

    /**
     * Returns validation errors for a candidate macro. An empty list means the macro is safe to save.
     *
     * @param existingMacros    all currently saved macros, used for action-key and phrase collision checks
     * @param originalActionKey the macro's {@code actionKey} before editing ({@code null} for new macros);
     *                          allows a macro to keep its own action key during an edit without triggering
     *                          a uniqueness error
     */
    public static List<String> validate(
            MacroDefinition candidate,
            List<MacroDefinition> existingMacros,
            String originalActionKey
    ) {
        List<String> errors = new ArrayList<>();
        if (candidate == null) {
            return List.of("Macro is missing.");
        }

        validateIdentity(candidate, existingMacros, originalActionKey, errors);
        validatePhrases(candidate, existingMacros, originalActionKey, errors);
        validateParameters(candidate, errors);
        validateSteps(candidate, existingMacros, errors);
        return List.copyOf(errors);
    }

    private static void validateIdentity(
            MacroDefinition candidate,
            List<MacroDefinition> existingMacros,
            String originalActionKey,
            List<String> errors
    ) {
        String actionKey = candidate.getActionKey();
        if (actionKey == null || actionKey.isBlank()) {
            errors.add("Action key is required.");
        } else {
            if (!SAFE_ID.matcher(actionKey).matches()) {
                errors.add("Action key may contain only letters, digits, underscore, dash, dot, or colon.");
            }
            if (builtInCommandIds().contains(normalize(actionKey))) {
                errors.add("Action key collides with a built-in command.");
            }
            for (MacroDefinition macro : safeMacros(existingMacros)) {
                if (!sameId(macro.getActionKey(), originalActionKey) && sameId(macro.getActionKey(), actionKey)) {
                    errors.add("Action key must be unique among macros.");
                    break;
                }
            }
        }

        if (candidate.getName() == null || candidate.getName().isBlank()) {
            errors.add("Name is required.");
        }
    }

    private static void validatePhrases(
            MacroDefinition candidate,
            List<MacroDefinition> existingMacros,
            String originalActionKey,
            List<String> errors
    ) {
        List<String> phrases = AiActionLocalizations.splitPhraseGroup(candidate.getPhrases());
        if (phrases.isEmpty()) {
            errors.add("At least one phrase is required.");
            return;
        }

        Set<String> builtInPhrases = builtInPhrases();
        Set<String> seen = new HashSet<>();
        for (String phrase : phrases) {
            String normalized = normalize(phrase);
            if (!seen.add(normalized)) {
                errors.add("Duplicate phrase: " + phrase);
            }
            if (builtInPhrases.contains(normalized)) {
                errors.add("Phrase collides with a built-in action alias: " + phrase);
            }
        }

        for (MacroDefinition macro : safeMacros(existingMacros)) {
            if (sameId(macro.getActionKey(), originalActionKey)) {
                continue;
            }
            Set<String> otherPhrases = normalizedPhrases(macro);
            for (String phrase : phrases) {
                if (otherPhrases.contains(normalize(phrase))) {
                    errors.add("Phrase collides with another macro: " + phrase);
                }
            }
        }
    }

    private static void validateParameters(MacroDefinition candidate, List<String> errors) {
        List<MacroParameterSpec> params = candidate.getParameters();
        if (params.isEmpty()) return;

        Set<String> seen = new HashSet<>();
        for (MacroParameterSpec param : params) {
            if (param == null || param.getName() == null || param.getName().isBlank()) {
                errors.add("Parameter name is required.");
                continue;
            }
            String normalizedName = param.getName().toLowerCase(Locale.ROOT);
            if (!VALID_PARAM_NAME.matcher(param.getName()).matches()) {
                errors.add("Parameter '" + param.getName() + "': name may only contain letters, digits, or underscore.");
            }
            if (!seen.add(normalizedName)) {
                errors.add("Duplicate parameter name: " + param.getName());
            }
            if (param.getType() == null || !MacroParameterSpec.VALID_TYPES.contains(param.getType())) {
                errors.add("Parameter '" + param.getName() + "': type must be one of "
                        + MacroParameterSpec.VALID_TYPES + ".");
            }
        }
    }

    private static void validateSteps(
            MacroDefinition candidate,
            List<MacroDefinition> existingMacros,
            List<String> errors
    ) {
        List<MacroStep> steps = candidate.getSteps();
        if (steps.isEmpty()) {
            errors.add("At least one step is required.");
            return;
        }

        Set<String> macroIds = new HashSet<>();
        for (MacroDefinition macro : safeMacros(existingMacros)) {
            macroIds.add(normalize(macro.getActionKey()));
        }
        macroIds.add(normalize(candidate.getActionKey()));

        // Build the set of declared parameter names for reference checking.
        Set<String> declaredParamNames = new HashSet<>();
        for (MacroParameterSpec spec : candidate.getParameters()) {
            if (spec != null && spec.getName() != null) {
                declaredParamNames.add(spec.getName());
            }
        }

        for (int i = 0; i < steps.size(); i++) {
            MacroStep step = steps.get(i);
            String prefix = "Step " + (i + 1) + ": ";
            if (step == null || step.getType() == null) {
                errors.add(prefix + "type is required.");
                continue;
            }
            switch (step.getType()) {
                case SPEAK -> {
                    requireText(step.getText(), prefix + "text is required.", errors);
                    validateParamRefs(step.getText(), prefix, "text", declaredParamNames, errors);
                }
                case BINDING_TAP -> requireText(step.getBindingId(), prefix + "bindingId is required.", errors);
                case BINDING_HOLD -> {
                    requireText(step.getBindingId(), prefix + "bindingId is required.", errors);
                    requirePositive(step.getDurationMs(), prefix + "durationMs must be positive.", errors);
                }
                case DELAY -> requirePositive(step.getDurationMs(), prefix + "durationMs must be positive.", errors);
                case RAW_KEY -> requireText(step.getRawKey(), prefix + "rawKey is required.", errors);
                case RUN_COMMAND -> {
                    requireText(step.getActionId(), prefix + "commandId is required.", errors);
                    if (macroIds.contains(normalize(step.getActionId()))) {
                        errors.add(prefix + "RUN_COMMAND cannot target another macro.");
                    }
                    step.getStepParams().forEach((key, template) ->
                            validateParamRefs(template, prefix, "stepParams[" + key + "]",
                                    declaredParamNames, errors));
                }
            }
        }
    }

    /** Checks that all {@code ${name}} references in {@code template} are declared macro parameters. */
    private static void validateParamRefs(
            String template, String stepPrefix, String fieldName,
            Set<String> declaredParamNames, List<String> errors
    ) {
        // Skip ref checks for parameterless macros — they cannot have ${…} refs by definition.
        if (template == null || declaredParamNames.isEmpty()) return;
        Matcher m = MacroExecutionContext.PARAM_REF.matcher(template);
        while (m.find()) {
            String ref = m.group(1);
            if (!declaredParamNames.contains(ref)) {
                errors.add(stepPrefix + fieldName + " references undeclared parameter '" + ref + "'.");
            }
        }
    }

    private static void requireText(String value, String message, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(message);
        }
    }

    private static void requirePositive(int value, String message, List<String> errors) {
        if (value <= 0) {
            errors.add(message);
        }
    }

    private static Set<String> builtInCommandIds() {
        Set<String> ids = new HashSet<>();
        for (Commands command : Commands.values()) {
            ids.add(normalize(command.getAction()));
        }
        return ids;
    }

    private static Set<String> builtInPhrases() {
        Map<String, String> aliases = new java.util.LinkedHashMap<>();
        AiActionLocalizations.addAliases(aliases, Status.getInstance(), true);
        Set<String> phrases = new HashSet<>();
        aliases.keySet().forEach(group ->
                AiActionLocalizations.splitPhraseGroup(group).forEach(phrase -> phrases.add(normalize(phrase))));
        return phrases;
    }

    private static Set<String> normalizedPhrases(MacroDefinition macro) {
        Set<String> phrases = new HashSet<>();
        AiActionLocalizations.splitPhraseGroup(macro.getPhrases()).forEach(phrase -> phrases.add(normalize(phrase)));
        return phrases;
    }

    private static List<MacroDefinition> safeMacros(List<MacroDefinition> macros) {
        return macros == null ? List.of() : macros;
    }

    private static boolean sameId(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}

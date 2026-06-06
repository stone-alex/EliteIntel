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
 * Validates macro definitions.
 * <p>
 * Two public entry points:
 * <ul>
 *   <li>{@link #validateFormat} — self-contained format checks (actionKey pattern, length,
 *       built-in collision). Used by the load path to reject individually malformed macros
 *       without requiring cross-macro context.</li>
 *   <li>{@link #validate} — full contextual validation including actionKey uniqueness,
 *       phrase collision, parameter and step cross-references. Used by the macro editor
 *       before saving.</li>
 * </ul>
 */
public final class MacroValidator {

    /** Strict snake_case: lowercase letters, digits, and underscores only. */
    static final Pattern SAFE_ID = Pattern.compile("[a-z0-9_]+");
    static final int MIN_ACTION_KEY_LENGTH = 10;
    static final int MAX_ACTION_KEY_LENGTH = 60;

    private static final Pattern VALID_PARAM_NAME = Pattern.compile("[A-Za-z0-9_]+");

    private MacroValidator() {
    }

    /**
     * Validates actionKey format rules that require no cross-macro context:
     * pattern, length, and built-in command collision.
     * Returns an empty list when all format rules pass.
     */
    public static List<String> validateFormat(MacroDefinition candidate) {
        if (candidate == null) {
            return List.of("Macro is missing.");
        }
        List<String> errors = new ArrayList<>();
        String actionKey = candidate.getActionKey();
        if (actionKey == null || actionKey.isBlank()) {
            errors.add("Action key is required.");
        } else {
            appendActionKeyFormatErrors(actionKey, errors);
            if (builtInCommandIds().contains(actionKey.toLowerCase(Locale.ROOT))) {
                errors.add("Action key collides with a built-in command.");
            }
        }
        return List.copyOf(errors);
    }

    /**
     * Full macro validation including cross-macro context.
     * Subsumes all checks from {@link #validateFormat} and additionally checks
     * actionKey uniqueness, phrase collisions, parameter names/types, and step
     * parameter references.
     *
     * @param existingMacros    all currently saved macros, used for uniqueness and phrase checks
     * @param originalActionKey the macro's {@code actionKey} before editing ({@code null} for
     *                          new macros); allows a macro to keep its own key during an edit
     *                          without triggering a uniqueness error
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
            appendActionKeyFormatErrors(actionKey, errors);
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

    /**
     * Appends pattern and length errors for {@code actionKey}.
     * Length is checked only when the pattern passes so both errors never appear together.
     */
    private static void appendActionKeyFormatErrors(String actionKey, List<String> errors) {
        if (!SAFE_ID.matcher(actionKey).matches()) {
            errors.add("Action key must use lowercase letters, numbers, and underscores only.");
        } else if (actionKey.length() < MIN_ACTION_KEY_LENGTH) {
            errors.add("Action key must be at least " + MIN_ACTION_KEY_LENGTH + " characters.");
        } else if (actionKey.length() > MAX_ACTION_KEY_LENGTH) {
            errors.add("Action key must not exceed " + MAX_ACTION_KEY_LENGTH + " characters.");
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

    static Set<String> builtInCommandIds() {
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

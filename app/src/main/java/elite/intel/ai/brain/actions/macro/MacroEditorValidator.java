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
import java.util.regex.Pattern;

/**
 * Validates editor-created macros before they are persisted or registered for command dispatch.
 */
public final class MacroEditorValidator {

    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9_.:-]+");

    private MacroEditorValidator() {
    }

    /**
     * Returns validation errors for a candidate macro. An empty list means the macro is safe to save.
     */
    public static List<String> validate(
            MacroDefinition candidate,
            List<MacroDefinition> existingMacros,
            String originalId
    ) {
        List<String> errors = new ArrayList<>();
        if (candidate == null) {
            return List.of("Macro is missing.");
        }

        validateIdentity(candidate, existingMacros, originalId, errors);
        validatePhrases(candidate, existingMacros, originalId, errors);
        validateSteps(candidate, existingMacros, errors);
        return List.copyOf(errors);
    }

    private static void validateIdentity(
            MacroDefinition candidate,
            List<MacroDefinition> existingMacros,
            String originalId,
            List<String> errors
    ) {
        String id = candidate.getId();
        if (id == null || id.isBlank()) {
            errors.add("Id is required.");
        } else {
            if (!SAFE_ID.matcher(id).matches()) {
                errors.add("Id may contain only letters, digits, underscore, dash, dot, or colon.");
            }
            if (builtInCommandIds().contains(normalize(id))) {
                errors.add("Id collides with a built-in command.");
            }
            for (MacroDefinition macro : safeMacros(existingMacros)) {
                if (!sameId(macro.getId(), originalId) && sameId(macro.getId(), id)) {
                    errors.add("Id must be unique among macros.");
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
            String originalId,
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
            if (sameId(macro.getId(), originalId)) {
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
            macroIds.add(normalize(macro.getId()));
        }
        macroIds.add(normalize(candidate.getId()));

        for (int i = 0; i < steps.size(); i++) {
            MacroStep step = steps.get(i);
            String prefix = "Step " + (i + 1) + ": ";
            if (step == null || step.getType() == null) {
                errors.add(prefix + "type is required.");
                continue;
            }
            switch (step.getType()) {
                case SPEAK -> requireText(step.getText(), prefix + "text is required.", errors);
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
                }
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

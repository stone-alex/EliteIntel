package elite.intel.tools;

import elite.intel.ai.hands.BindingsLoader;
import elite.intel.ai.hands.KeyBindingsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.TreeMap;

public class HelpGenerateBindings {
    private static final Logger log = LoggerFactory.getLogger(HelpGenerateBindings.class);
    private static final Set<String> BLACKLISTED_ACTIONS = new HashSet<>(Arrays.asList(
            "PrimaryFire", "SecondaryFire", "TriggerFieldNeutraliser",
            "BuggyPrimaryFireButton", "BuggySecondaryFireButton"
    ));
    public static final String FILE_NAME = "KeyboardBindingsMapping.txt";

    public static void main(String[] args) {
        try {
            KeyBindingsParser parser = KeyBindingsParser.getInstance();
            File bindingsFile = new BindingsLoader().getLatestBindsFile();
            Map<String, KeyBindingsParser.KeyBinding> bindings = new TreeMap<>(parser.parseBindings(bindingsFile));

            StringBuilder sb = new StringBuilder();
            sb.append("####################################################################\n");
            sb.append("# Enum cheatsheet. Provides game bindings in form of enum\n");
            sb.append("# Enum name, AI/User facing command, Game Keybind\n");
            sb.append("####################################################################\n\n");
            boolean first = true;
            for (String binding : bindings.keySet()) {
                if (BLACKLISTED_ACTIONS.contains(binding) || binding.startsWith("Humanoid")) {
                    log.debug("Skipping blacklisted or Humanoid action: {}", binding);
                    continue;
                }

                String userCommand = generateUserFriendlyCommand(binding);
                if (!first) {
                    sb.append(",\n");
                }
                sb.append(String.format("%s(\"%s\", \"%s\")",toEnumName(userCommand), userCommand, binding));
                first = false;
            }
            
            File outputFile = new File(FILE_NAME);
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(sb.toString());
                log.info("Generated {} at {}", FILE_NAME, outputFile.getAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to write {}: {}", FILE_NAME, e.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to generate GameCommandMapping: {}", e.getMessage());
        }
    }

    private static String generateUserFriendlyCommand(String binding) {
        // Convert CamelCase to snake_case
        StringBuilder result = new StringBuilder();
        boolean lastWasUpper = false;
        for (char c : binding.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (lastWasUpper) {
                    result.append(Character.toLowerCase(c));
                } else {
                    result.append("_").append(Character.toLowerCase(c));
                }
                lastWasUpper = true;
            } else {
                result.append(c);
                lastWasUpper = false;
            }
        }
        String snakeCase = result.toString().replaceAll("^_", "");

        // Simplify verbs and terms
        snakeCase = snakeCase.replace("toggle_", "");
        snakeCase = snakeCase.replace("order_", "request_");
        snakeCase = snakeCase.replace("_open", "");
        snakeCase = snakeCase.replace("hyperspace", "jump_to_hyperspace");
        snakeCase = snakeCase.replace("supercruise", "enter_supercruise");

        return snakeCase;
    }

    private static String toEnumName(String userCommand) {
        return userCommand.toUpperCase().replace("_", "_");
    }
}
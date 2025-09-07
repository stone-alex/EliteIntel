package elite.companion.tools;

import elite.companion.ai.hands.BindingsLoader;
import elite.companion.ai.hands.KeyBindingsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The GenerateGameCommandMapping class is responsible for generating a Java class that maps
 * game command actions to user-friendly command names. It includes parsing key bindings
 * from a configuration file, filtering out blacklisted actions, and generating a mapping
 * as a Java enum structure that can be used in other parts of the application.
 * <p>
 * The generated output is written to a predefined file path and includes methods for:
 * - Retrieving game bindings based on user commands.
 * - Listing all user commands available in the mapping.
 * <p>
 * The class uses a specified blacklist of actions and ignores actions starting with the
 * "Humanoid" prefix while processing the key bindings. It also ensures that mappings are
 * formatted in a user-friendly, readable manner by converting CamelCase into snake_case and
 * applying specific naming transformations.
 * <p>
 * Key functionality includes:
 * - Reading the latest key bindings configuration file.
 * - Parsing actions and their associated bindings.
 * - Generating a class with enum definitions for game commands.
 * - Writing the generated class to a file for use in the application.
 */
public class GenerateGameCommandMapping {
    private static final Logger log = LoggerFactory.getLogger(GenerateGameCommandMapping.class);
    private static final Set<String> BLACKLISTED_ACTIONS = new HashSet<>(Arrays.asList(
            "PrimaryFire", "SecondaryFire", "TriggerFieldNeutraliser",
            "BuggyPrimaryFireButton", "BuggySecondaryFireButton"
    ));

    public static void main(String[] args) {
        try {
            KeyBindingsParser parser = KeyBindingsParser.getInstance();
            File bindingsFile = new BindingsLoader().getLatestBindsFile();
            Map<String, KeyBindingsParser.KeyBinding> bindings = parser.parseBindings(bindingsFile);

            StringBuilder enumBuilder = new StringBuilder();
            enumBuilder.append("package elite.companion.comms;\n\n");
            enumBuilder.append("public class GameCommandMapping {\n");
            enumBuilder.append("    public enum GameCommand {\n");

            boolean first = true;
            for (String binding : bindings.keySet()) {
                if (BLACKLISTED_ACTIONS.contains(binding) || binding.startsWith("Humanoid")) {
                    log.debug("Skipping blacklisted or Humanoid action: {}", binding);
                    continue;
                }

                String userCommand = generateUserFriendlyCommand(binding);
                if (!first) {
                    enumBuilder.append(",\n");
                }
                enumBuilder.append(String.format("        %s(\"%s\", \"%s\")",
                        toEnumName(userCommand), userCommand, binding));
                first = false;
            }

            enumBuilder.append(";\n\n");
            enumBuilder.append("        private final String userCommand;\n");
            enumBuilder.append("        private final String gameBinding;\n\n");
            enumBuilder.append("        GameCommand(String userCommand, String gameBinding) {\n");
            enumBuilder.append("            this.userCommand = userCommand;\n");
            enumBuilder.append("            this.gameBinding = gameBinding;\n");
            enumBuilder.append("        }\n\n");
            enumBuilder.append("        public String getUserCommand() {\n");
            enumBuilder.append("            return userCommand;\n");
            enumBuilder.append("        }\n\n");
            enumBuilder.append("        public String getGameBinding() {\n");
            enumBuilder.append("            return gameBinding;\n");
            enumBuilder.append("        }\n");
            enumBuilder.append("    }\n\n");
            enumBuilder.append("    public static String getGameBinding(String userCommand) {\n");
            enumBuilder.append("        for (GameCommand command : GameCommand.values()) {\n");
            enumBuilder.append("            if (command.getUserCommand().equals(userCommand)) {\n");
            enumBuilder.append("                return command.getGameBinding();\n");
            enumBuilder.append("            }\n");
            enumBuilder.append("        }\n");
            enumBuilder.append("        return null;\n");
            enumBuilder.append("    }\n\n");
            enumBuilder.append("    public static String[] getUserCommands() {\n");
            enumBuilder.append("        String[] commands = new String[GameCommand.values().length];\n");
            enumBuilder.append("        for (int i = 0; i < GameCommand.values().length; i++) {\n");
            enumBuilder.append("            commands[i] = GameCommand.values()[i].getUserCommand();\n");
            enumBuilder.append("        }\n");
            enumBuilder.append("        return commands;\n");
            enumBuilder.append("    }\n");
            enumBuilder.append("}\n");

            File outputFile = new File("app/src/main/java/elite/companion/comms/GameCommandMapping.java");
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(enumBuilder.toString());
                log.info("Generated GameCommandMapping.java at {}", outputFile.getAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to write GameCommandMapping.java: {}", e.getMessage());
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
        snakeCase = snakeCase.replace("supercruise", "engage_supercruise");

        return snakeCase;
    }

    private static String toEnumName(String userCommand) {
        return userCommand.toUpperCase().replace("_", "_");
    }
}
package elite.companion.tools;

import elite.companion.robot.BindingsLoader;
import elite.companion.robot.KeyBindingsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenerateGameCommandMapping {
    private static final Logger log = LoggerFactory.getLogger(GenerateGameCommandMapping.class);
    private static final Set<String> BLACKLISTED_ACTIONS = new HashSet<>(Arrays.asList(
            "PrimaryFire", "SecondaryFire", "TriggerFieldNeutraliser",
            "BuggyPrimaryFireButton", "BuggySecondaryFireButton"
    ));

    public static void main(String[] args) {
        try {
            KeyBindingsParser parser = new KeyBindingsParser();
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
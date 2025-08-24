package elite.companion.comms;

import elite.companion.robot.BindingsLoader;
import elite.companion.robot.KeyBindingsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GrokRequestHints {
    private static final Logger log = LoggerFactory.getLogger(GrokRequestHints.class);
    static final List<String> COMMANDS;
    static final List<String> COMMON_PHRASES = Arrays.asList(
            "tritium", "mining", "material trader", "plot route", "supercruise", "landing gear", "cargo scoop",
            "request docking"
    );
    static final List<String> DOMAIN_CONCEPTS = Arrays.asList(
            "carrier fuel", "asteroid prospecting", "galaxy map"
    );
    static final List<String> SUPPORTED_QUERIES = Arrays.asList(
            CommandAction.FIND_NEAREST_MATERIAL_TRADER.getAction()
    );

    static {
        List<String> commands = Arrays.stream(CommandAction.values())
                .map(CommandAction::getCommandWithPlaceholder)
                .collect(Collectors.toList());

        // Add user-friendly game commands
        commands.addAll(Arrays.asList(GameCommandMapping.getUserCommands()));

        // Load game bindings
        try {
            KeyBindingsParser parser = new KeyBindingsParser();
            File bindingsFile = new BindingsLoader().getLatestBindsFile();
            Map<String, KeyBindingsParser.KeyBinding> bindings = parser.parseBindings(bindingsFile);

            Set<String> strings = bindings.keySet();
            for(String s : strings) {
                if(!s.toLowerCase().contains("camera") || s.toLowerCase().contains("cam")) {
                    commands.add(s);
                }
            }

            //commands.addAll(bindings.keySet());


            log.info("Loaded {} game bindings for Grok hints", bindings.size());
        } catch (Exception e) {
            log.error("Failed to load game bindings for hints: {}", e.getMessage());
        }

        COMMANDS = commands;
    }

    private static String formatCommands() {
        return "Supported commands: '" + String.join("', '", COMMANDS) + "'. ";
    }

    public static final String supportedCommands = formatCommands();
}
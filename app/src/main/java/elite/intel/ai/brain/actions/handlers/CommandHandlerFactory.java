package elite.intel.ai.brain.actions.handlers;

import elite.intel.ai.brain.actions.Commands;
import elite.intel.ai.brain.actions.handlers.commands.CommandHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class CommandHandlerFactory {

    private static final Logger log = LogManager.getLogger(CommandHandlerFactory.class);
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private static CommandHandlerFactory instance;

    private CommandHandlerFactory() {
    }

    public static CommandHandlerFactory getInstance() {
        if (instance == null) {
            instance = new CommandHandlerFactory();
        }
        return instance;
    }

    public Map<String, CommandHandler> registerCommandHandlers() {
        for (Commands action : Commands.values()) {
            try {
                CommandHandler handler = instantiateCommandHandler(action.getHandlerClass(), action.getAction());
                commandHandlers.put(action.getAction(), handler);
                log.debug("Registered custom command handler for action: {}", action.getAction());
            } catch (Exception e) {
                log.error("Failed to register custom command handler for action: {}", action.getAction(), e);
                throw new RuntimeException("Custom command handler registration failed for action: " + action.getAction(), e);
            }
        }

        return commandHandlers;
    }

    public Map<String, CommandHandler> getCommandHandlers() {
        return commandHandlers;
    }

    private CommandHandler instantiateCommandHandler(Class<? extends CommandHandler> handlerClass, String action) {
        try {
            Constructor<? extends CommandHandler> constructor = handlerClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            log.error("No suitable constructor found for handler: {}, action/binding: {}", handlerClass.getName(), action);
            throw new RuntimeException("Failed to instantiate command handler: " + handlerClass.getName(), e);
        } catch (Exception e) {
            log.error("Failed to instantiate command handler: {}, action/binding: {}", handlerClass.getName(), action, e);
            throw new RuntimeException("Failed to instantiate command handler: " + handlerClass.getName(), e);
        }
    }
}

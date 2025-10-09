package elite.intel.ai.brain.handlers;

import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.commands.custom.Commands;
import elite.intel.ai.hands.GameController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class CommandHandlerFactory {

    private static final Logger log = LogManager.getLogger(CommandHandlerFactory.class);
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private static CommandHandlerFactory instance;
    private final GameController gameHandler;

    private CommandHandlerFactory() {
        // Private constructor for singleton
        try {
            this.gameHandler = new GameController();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CommandHandlerFactory getInstance() {
        if (instance == null) {
            instance = new CommandHandlerFactory();
        }
        return instance;
    }

    public GameController getGameCommandHandler() {
        return gameHandler;
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

    private CommandHandler instantiateCommandHandler(Class<? extends CommandHandler> handlerClass, String actionOrBinding) {
        try {
            try {
                Constructor<? extends CommandHandler> constructor = handlerClass.getDeclaredConstructor(GameController.class);
                constructor.setAccessible(true);
                return constructor.newInstance(gameHandler);
            } catch (NoSuchMethodException e) {
                Constructor<? extends CommandHandler> constructor = handlerClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            }
        } catch (NoSuchMethodException e) {
            log.error("No suitable constructor found for handler: {}, action/binding: {}", handlerClass.getName(), actionOrBinding);
            throw new RuntimeException("Failed to instantiate command handler: " + handlerClass.getName(), e);
        } catch (Exception e) {
            log.error("Failed to instantiate command handler: {}, action/binding: {}", handlerClass.getName(), actionOrBinding, e);
            throw new RuntimeException("Failed to instantiate command handler: " + handlerClass.getName(), e);
        }
    }
}

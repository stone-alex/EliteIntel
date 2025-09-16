package elite.intel.ai.brain.handlers;

import elite.intel.ai.brain.handlers.commands.CommandHandler;
import elite.intel.ai.brain.handlers.commands.GameCommands;
import elite.intel.ai.brain.handlers.commands.GenericGameController;
import elite.intel.ai.brain.handlers.commands.custom.CustomCommands;
import elite.intel.ai.hands.GameHandler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class CommandHandlerFactory {

    private static final Logger log = LogManager.getLogger(CommandHandlerFactory.class);
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private static CommandHandlerFactory instance;
    private final GameHandler _gameHandler;

    private CommandHandlerFactory() {
        // Private constructor for singleton
        try {
            this._gameHandler = new GameHandler();
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

    public GameHandler getGameCommandHandler() {
        return _gameHandler;
    }


    public Map<String, CommandHandler> registerCommandHandlers() {
        for (CustomCommands action : CustomCommands.values()) {
            try {
                CommandHandler handler = instantiateCommandHandler(action.getHandlerClass(), action.getAction());
                commandHandlers.put(action.getAction(), handler);
                log.debug("Registered custom command handler for action: {}", action.getAction());
            } catch (Exception e) {
                log.error("Failed to register custom command handler for action: {}", action.getAction(), e);
                throw new RuntimeException("Custom command handler registration failed for action: " + action.getAction(), e);
            }
        }

        for (GameCommands.GameCommand command : GameCommands.GameCommand.values()) {
            try {
                CommandHandler handler = instantiateCommandHandler(command.getHandlerClass(), command.getUserCommand());
                commandHandlers.put(command.getUserCommand(), handler);
                log.debug("Registered game command handler for binding: {}", command.getUserCommand());
            } catch (Exception e) {
                log.error("Failed to register game command handler for binding: {}", command.getUserCommand(), e);
                throw new RuntimeException("Game command handler registration failed for binding: " + command.getUserCommand(), e);
            }
        }
        return commandHandlers;
    }

    private CommandHandler instantiateCommandHandler(Class<? extends CommandHandler> handlerClass, String actionOrBinding) {
        try {
            if (handlerClass == GenericGameController.class) {
                Constructor<? extends CommandHandler> constructor = handlerClass.getDeclaredConstructor(GameHandler.class, String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(_gameHandler, actionOrBinding);
            } else {
                try {
                    Constructor<? extends CommandHandler> constructor = handlerClass.getDeclaredConstructor(GameHandler.class);
                    constructor.setAccessible(true);
                    return constructor.newInstance(_gameHandler);
                } catch (NoSuchMethodException e) {
                    Constructor<? extends CommandHandler> constructor = handlerClass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    return constructor.newInstance();
                }
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

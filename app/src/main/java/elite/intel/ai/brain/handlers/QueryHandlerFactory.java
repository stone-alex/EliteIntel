package elite.intel.ai.brain.handlers;

import elite.intel.ai.brain.handlers.query.Queries;
import elite.intel.ai.brain.handlers.query.QueryHandler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory class for managing the registration and instantiation of {@link QueryHandler} instances.
 * Implements the singleton design pattern to ensure a single instance is used across the application.
 */
public class QueryHandlerFactory {

    private static final Logger log = LogManager.getLogger(QueryHandlerFactory.class);
    private static QueryHandlerFactory instance;
    private final Map<String, QueryHandler> queryHandlers = new HashMap<>();

    private QueryHandlerFactory() {
        // Private constructor for singleton
    }

    public static QueryHandlerFactory getInstance() {
        if (instance == null) {
            instance = new QueryHandlerFactory();
        }
        return instance;
    }


    /**
     * Registers query handlers for all defined actions in the {@code QueryActions} enum.
     * Each handler is instantiated using its defined handler class and stored in the internal map
     * with the corresponding action as the key.
     * <p>
     * In the event of an instantiation failure for a specific handler, a runtime exception is thrown
     * and the registration process for that action is halted.
     *
     * @return A map containing registered query handlers, where the keys are action strings and the values
     * are instances of {@link QueryHandler}.
     * @throws RuntimeException if a handler cannot be instantiated or registered for any action.
     */
    public Map<String, QueryHandler> registerQueryHandlers() {
        for (Queries action : Queries.values()) {
            try {
                QueryHandler handler = instantiateHandler(action.getHandlerClass(), QueryHandler.class);
                queryHandlers.put(action.getAction(), handler);
                log.debug("Registered query handler for action: {}", action.getAction());
            } catch (Exception e) {
                log.error("Failed to register query handler for action: {}", action.getAction(), e);
                throw new RuntimeException("Query handler registration failed for action: " + action.getAction(), e);
            }
        }
        return queryHandlers;
    }

    private <T> T instantiateHandler(Class<? extends T> handlerClass, Class<T> expectedType) {
        try {
            Constructor<? extends T> constructor = handlerClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T handler = constructor.newInstance();
            if (!expectedType.isInstance(handler)) {
                throw new IllegalStateException("Handler class " + handlerClass.getName() + " does not implement " + expectedType.getName());
            }
            return handler;
        } catch (NoSuchMethodException e) {
            log.error("No no-arg constructor found for handler: {}", handlerClass.getName());
            throw new RuntimeException("Failed to instantiate handler: " + handlerClass.getName(), e);
        } catch (Exception e) {
            log.error("Failed to instantiate handler: {}", handlerClass.getName(), e);
            throw new RuntimeException("Failed to instantiate handler: " + handlerClass.getName(), e);
        }
    }
}

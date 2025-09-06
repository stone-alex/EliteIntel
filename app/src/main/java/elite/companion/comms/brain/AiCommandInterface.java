package elite.companion.comms.brain;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.UserInputEvent;

/**
 * The AiCommandInterface represents a contract for managing the lifecycle and event handling
 * of an AI command system. It defines methods to start and stop the system, as well as to
 * react to specific events such as user input and sensor data updates.
 * <p>
 * Implementations of this interface are expected to integrate with event subscription mechanisms
 * such as elite.companion.gameapi.SubscriberRegistration or EventBusManager to handle incoming events.
 * These events allow the AI system to process user inputs and sensor state updates, which are
 * essential for generating dynamic and context-aware responses.
 * <p>
 * Methods:
 * - start(): Initializes the AI command system. Should handle setup logic and throw an exception if initialization fails.
 * - stop(): Stops the AI command system and performs any necessary cleanup operations.
 * - onUserInput(UserInputEvent event): Handles user input events. The implementation should process the sanitized user input and associated confidence score.
 * - onSensorDataEvent(SensorDataEvent event): Processes sensor data events. This provides opportunities to update the system with environmental or contextual data.
 */
public interface AiCommandInterface {
    void start() throws Exception;
    void stop();

    /**
     * Use elite.companion.gameapi.SubscriberRegistration to subscribe to these events
     * or add EventBusManager.register(this); in the constructor if your class is singleton
     */
    @Subscribe void onUserInput(UserInputEvent event);

    /**
     * Use elite.companion.gameapi.SubscriberRegistration to subscribe to these events
     * or add EventBusManager.register(this); in the constructor if your class is singleton
     */
    @Subscribe void onSensorDataEvent(SensorDataEvent event);
}

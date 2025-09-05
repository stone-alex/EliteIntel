package elite.companion.comms.brain;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.UserInputEvent;

public interface AiCommandInterface {
    void start() throws Exception;
    void stop();

    /**
     * Use EventBusManager to subscribe to these events
     *
     */
    @Subscribe void onUserInput(UserInputEvent event);

    /**
     * Use EventBusManager to subscribe to these events
     *
     */
    @Subscribe void onSensorDataEvent(SensorDataEvent event);
}

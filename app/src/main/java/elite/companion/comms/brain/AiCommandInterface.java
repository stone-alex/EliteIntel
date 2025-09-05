package elite.companion.comms.brain;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.UserInputEvent;

public interface AiCommandInterface {
    void start() throws Exception;
    void stop();

    /**
     * Use elite.companion.util.SubscriberRegistration to subscribe to these events
     * or add EventBusManager.register(this); in the constructor if your class is singleton
     */
    @Subscribe void onUserInput(UserInputEvent event);

    /**
     * Use elite.companion.util.SubscriberRegistration to subscribe to these events
     * or add EventBusManager.register(this); in the constructor if your class is singleton
     */
    @Subscribe void onSensorDataEvent(SensorDataEvent event);
}

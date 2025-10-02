package elite.intel.ai.mouth;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.VocalisationRequestEvent;

public interface MouthInterface {
    void start();
    void stop();

    void interruptAndClear();
    /**
     * Use elite.intel.gameapi.SubscriberRegistration to subscribe to these events
     * or add EventBusManager.register(this); in the constructor if your class is a singleton
     */
    @Subscribe void onVoiceProcessEvent(VocalisationRequestEvent event);
}

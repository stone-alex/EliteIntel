package elite.companion.comms.mouth;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.VoiceProcessEvent;

public interface MouthInterface {
    void start();
    void stop();

    /**
     * Use elite.companion.util.SubscriberRegistration to subscribe to these events
     * or add EventBusManager.register(this); in the constructor if your class is singleton
     */
    @Subscribe void onVoiceProcessEvent(VoiceProcessEvent event);
}

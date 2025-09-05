package elite.companion.comms.mouth;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.VoiceProcessEvent;

public interface MouthInterface {
    void start();
    void stop();
    @Subscribe void onVoiceProcessEvent(VoiceProcessEvent event);
}

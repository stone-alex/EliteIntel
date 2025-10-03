package elite.intel.ai.ears.google.subscriber;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ApiFactory;
import elite.intel.ai.ears.EarsInterface;
import elite.intel.ai.ears.google.GoogleSTTConnectionFailed;
import elite.intel.ai.mouth.subscribers.events.MissionCriticalAnnouncementEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.util.SleepNoThrow;

public class GoogleSttConnectionFailedHandler {

    @Subscribe
    public void onGoogleSttConnectionFailed(GoogleSTTConnectionFailed event) {
        EventBusManager.publish(new MissionCriticalAnnouncementEvent("Warning! Google STT connection failed. Restarting Ears"));
        EarsInterface earsImpl = ApiFactory.getInstance().getEarsImpl();
        earsImpl.stop();
        SleepNoThrow.sleep(2000);
        earsImpl.start();
    }
}

package elite.intel.gameapi.gamestate.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.gameapi.gamestate.status_events.BeingInterdictedEvent;
import elite.intel.session.Status;

public class InterdictionHandler {


    private final Status status = Status.getInstance();

    @Subscribe
    public void onInterdictedEvent(BeingInterdictedEvent event) {
        boolean analysisMode = status.isAnalysisMode();
        if (analysisMode) {
            EventBusManager.publish(new UserInputEvent("activate combat mode", 100));
            EventBusManager.publish(new UserInputEvent("target highest threat", 100));
        }
    }
}

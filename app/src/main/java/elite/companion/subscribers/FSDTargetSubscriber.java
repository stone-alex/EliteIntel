package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.FSDTargetEvent;
import elite.companion.gameapi.events.NavRouteDto;
import elite.companion.session.SystemSession;

import java.util.Map;

public class FSDTargetSubscriber {

    public FSDTargetSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onFSDTargetEvent(FSDTargetEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.clearFssSignals();
        String fsd_target = String.valueOf(systemSession.getObject(SystemSession.FSD_TARGET));
        if (fsd_target != null && !fsd_target.isEmpty()) {
            Map<String, NavRouteDto> route = systemSession.getRoute();
            if (route != null && !route.isEmpty()) {
                NavRouteDto firstStop = route.values().iterator().next();
                VoiceGenerator.getInstance().speak("Jumping to " + firstStop.getName() + isFuelStarClause(event.getStarClass()));
            }
        }
        systemSession.updateSession(SystemSession.FSD_TARGET, event.getName());
    }

    private String isFuelStarClause(String starClass) {
        boolean isFuelStar = "KGBFOAM".contains(starClass);
        return isFuelStar ? " (Fuel Star)" : "";
    }
}

package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.gameapi.journal.events.FSDTargetEvent;
import elite.companion.session.SystemSession;

import java.util.Map;

@SuppressWarnings("unused")
public class FSDTargetSubscriber {

    @Subscribe
    public void onFSDTargetEvent(FSDTargetEvent event) {
        SystemSession systemSession = SystemSession.getInstance();
        systemSession.clearFssSignals();
        String fsd_target = String.valueOf(systemSession.getObject(SystemSession.FSD_TARGET));
        if (fsd_target != null && !fsd_target.isEmpty()) {
            Map<String, NavRouteDto> route = systemSession.getRoute();
            if (route != null && !route.isEmpty()) {
                NavRouteDto firstStop = route.values().iterator().next();
                VoiceGenerator.getInstance().speak("Jumping to " + firstStop.getName() + isFuelStarClause(firstStop.getStarClass()));
            }
        }
        systemSession.updateSession(SystemSession.FSD_TARGET, event.getName());
    }

    private String isFuelStarClause(String starClass) {
        boolean isFuelStar = "KGBFOAM".contains(starClass);
        return isFuelStar ? " (Fuel Star)" : "";
    }
}

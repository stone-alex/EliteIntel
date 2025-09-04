package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.gamestate.events.NavRouteDto;
import elite.companion.gameapi.journal.events.FSDTargetEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.ui.event.AppLogEvent;
import elite.companion.util.EventBusManager;

import java.util.Map;

@SuppressWarnings("unused")
public class FSDTargetSubscriber {

    @Subscribe
    public void onFSDTargetEvent(FSDTargetEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.clearFssSignals();

        String fsdTarget = event.getName()+ isFuelStarClause(event.getStarClass());

        EventBusManager.publish(new AppLogEvent("Processing FSDTargetEvent. Storing in session only: " +fsdTarget));
        playerSession.put(PlayerSession.FSD_TARGET, fsdTarget);
    }

    private String isFuelStarClause(String starClass) {
        boolean isFuelStar = "KGBFOAM".toUpperCase().contains(starClass.toUpperCase());
        return isFuelStar ? " (Fuel Star)" : "";
    }
}

package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.EventBusManager;
import elite.companion.gameapi.journal.events.FSDTargetEvent;
import elite.companion.session.PlayerSession;
import elite.companion.ui.event.AppLogEvent;

@SuppressWarnings("unused")
public class FSDTargetSubscriber {

    @Subscribe
    public void onFSDTargetEvent(FSDTargetEvent event) {
        PlayerSession playerSession = PlayerSession.getInstance();
        String fsdTarget = event.getName() + isFuelStarClause(event.getStarClass());
        EventBusManager.publish(new AppLogEvent("Processing FSDTargetEvent. Storing in session only: " + fsdTarget));
        playerSession.put(PlayerSession.FSD_TARGET, fsdTarget);
    }

    private String isFuelStarClause(String starClass) {
        boolean isFuelStar = "KGBFOAM".toUpperCase().contains(starClass.toUpperCase());
        return isFuelStar ? " (Fuel Star)" : "";
    }
}

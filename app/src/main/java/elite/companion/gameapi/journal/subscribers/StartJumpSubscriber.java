package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.journal.events.StartJumpEvent;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class StartJumpSubscriber {

    @Subscribe
    public void onStartJumpEvent(StartJumpEvent event) {
        String jumpingTo = event.getStarSystem();
        String starClass = event.getStarClass();
        boolean scoopable = event.isScoopable();

        StringBuilder sb = new StringBuilder();
        sb.append("Jumping: ");
        sb.append(" ");
        sb.append("Star System: ");
        sb.append(jumpingTo);
        sb.append(", ");
        sb.append("Star Class: ");
        sb.append(starClass);
        sb.append(", ");
        sb.append(isFuelStarClause(starClass));

        if(!"Supercruise".equalsIgnoreCase(event.getJumpType())) {
            SystemSession.getInstance().sendToAiAnalysis(sb.toString());
        }
    }

    private String isFuelStarClause(String starClass) {
        boolean isFuelStar = "KGBFOAM".contains(starClass);
        return isFuelStar ? " Fuel Star" : " Warning! - Not a Fuel Star!";
    }

}

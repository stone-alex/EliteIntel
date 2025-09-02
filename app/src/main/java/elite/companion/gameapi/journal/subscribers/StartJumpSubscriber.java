package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.StartJumpEvent;
import elite.companion.session.SystemSession;
import elite.companion.ui.event.AppLogEvent;
import elite.companion.util.EventBusManager;

@SuppressWarnings("unused")
public class StartJumpSubscriber {

    @Subscribe
    public void onStartJumpEvent(StartJumpEvent event) {
        String jumpingTo = event.getStarSystem();
        String starClass = event.getStarClass();
        boolean scoopable = event.isScoopable();

        StringBuilder sb = new StringBuilder();
        sb.append("Jumping to: ");
        sb.append(" ");
        sb.append("Star System: ");
        sb.append(jumpingTo);
        sb.append(", ");
        sb.append("Star Class: ");
        sb.append(starClass);
        sb.append(", ");
        sb.append(isFuelStarClause(starClass));

        SystemSession.getInstance().put(SystemSession.JUMPING_TO, jumpingTo);

        if(!"Supercruise".equalsIgnoreCase(event.getJumpType())) {
            EventBusManager.publish(new AppLogEvent("Processing Event: StartJumpEvent sending sensor data to AI: "+sb.toString()));
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        }
    }

    private String isFuelStarClause(String starClass) {
        boolean isFuelStar = "KGBFOAM".contains(starClass);
        return isFuelStar ? " Fuel Star" : " Warning! - Not a Fuel Star!";
    }

}

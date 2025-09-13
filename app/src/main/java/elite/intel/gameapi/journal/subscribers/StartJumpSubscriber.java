package elite.intel.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.gameapi.journal.events.StartJumpEvent;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.AppLogEvent;

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

        PlayerSession playerSession = PlayerSession.getInstance();
        playerSession.put(PlayerSession.JUMPING_TO, jumpingTo);

        if (!"Supercruise".equalsIgnoreCase(event.getJumpType())) {
            EventBusManager.publish(new AppLogEvent("Processing Event: StartJumpEvent sending sensor data to AI: " + sb));
            EventBusManager.publish(new SensorDataEvent(sb.toString()));
        }

        playerSession.clearLocalInfo();
        playerSession.clearStellarObjects();
        playerSession.clearShipScans();

        playerSession.removeNavPoint(String.valueOf(playerSession.get(PlayerSession.CURRENT_SYSTEM_NAME)));
    }

    private String isFuelStarClause(String starClass) {
        if (starClass == null) {
            return "";
        }
        boolean isFuelStar = "KGBFOAM".contains(starClass);
        return isFuelStar ? " Fuel Star" : " Warning! - Not a Fuel Star!";
    }
}

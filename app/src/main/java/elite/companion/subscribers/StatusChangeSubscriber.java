package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.gameapi.events.GameEvents;
import elite.companion.session.SystemSession;

public class StatusChangeSubscriber {

    public StatusChangeSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onStatusChange(GameEvents.StatusEvent event) {
        long currentBalance = event.getBalance();
        double onBoardCargo = event.getCargo();
        String fireGroup = toFireGroupDesignation(event.getFireGroup());
        int[] pips = event.getPips();
        String powerDistribution = "Systems: " + (pips[0] * 12.5) + " percent " + ", Shields: " + (pips[1] * 12.5) + " percent " + ", Engines: " + (pips[2] * 12.5) + " percent";
        String localLegalStatus = event.getLegalState();


        String destinationLocked = event.getDestination() == null ? null : event.getDestination().getName();
        if (destinationLocked != null) {
            SystemSession systemSession = SystemSession.getInstance();
            String currentDestination = String.valueOf(systemSession.getObject(SystemSession.DESTINATION_TARGET));
            if (currentDestination != null && currentDestination.equals(destinationLocked)) {
                systemSession.updateSession(SystemSession.DESTINATION_TARGET, destinationLocked);
                systemSession.setSensorData("Destination locked: " + destinationLocked);
            }
        }

    }

    private String toFireGroupDesignation(int fireGroup) {
        if (fireGroup == 0) return "A";
        if (fireGroup == 1) return "B";
        if (fireGroup == 2) return "C";
        if (fireGroup == 3) return "D";
        if (fireGroup == 4) return "E";
        if (fireGroup == 5) return "F";
        if (fireGroup == 6) return "G";
        if (fireGroup == 7) return "H";
        if (fireGroup == 8) return "I";
        if (fireGroup == 9) return "J";
        if (fireGroup == 10) return "K";
        if (fireGroup == 11) return "L";


        return "Unknown";
    }
}

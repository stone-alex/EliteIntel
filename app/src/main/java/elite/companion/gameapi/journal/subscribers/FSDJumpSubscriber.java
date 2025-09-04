package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.journal.events.FSDJumpEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;
import elite.companion.ui.event.AppLogEvent;
import elite.companion.util.EventBusManager;

import java.util.List;

@SuppressWarnings("unused")
public class FSDJumpSubscriber {

    @Subscribe
    public void onFSDJumpEvent(FSDJumpEvent event) {
        PlayerSession systemSession = PlayerSession.getInstance();
        systemSession.put(PlayerSession.CURRENT_SYSTEM_DATA, event.toJson());

        String currentStarSystem = event.getStarSystem();

        boolean roueSet = !systemSession.getRoute().isEmpty();
        systemSession.removeNavPoint(currentStarSystem);
        String finalDestination = String.valueOf(systemSession.get(PlayerSession.FINAL_DESTINATION));
        String arrivedAt = String.valueOf(systemSession.get(PlayerSession.JUMPING_TO));
        systemSession.put(PlayerSession.CURRENT_SYSTEM, arrivedAt);

        StringBuilder sb = new StringBuilder();
        sb.append("Hyperspace Jump Successful: ");
        sb.append("We are in: ").append(currentStarSystem).append(" system, ");

        if (finalDestination != null && finalDestination.equalsIgnoreCase(currentStarSystem)) {
            sb.append("Arrived at final destination: ").append(finalDestination);
        } else {

            if (roueSet) {
                int remainingJump = systemSession.getRoute().size();

                if (remainingJump > 0) {
                    sb.append("next stop: ").append(systemSession.get(systemSession.FSD_TARGET)).append(", ");
                }

                sb.append(remainingJump).append(" jumps remaining: ").append(" to ").append(finalDestination).append(".");
            }
        }

        EventBusManager.publish(new AppLogEvent("Processing Event: FSDJumpEvent sending sensor data to AI: "+sb.toString()));
        EventBusManager.publish(new SensorDataEvent(sb.toString()));
    }
}

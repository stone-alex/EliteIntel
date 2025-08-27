package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.StartJumpEvent;
import elite.companion.session.SystemSession;

public class StartJumpSubscriber {

    public StartJumpSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onStartJumpEvent(StartJumpEvent event) {
        String jumpingTo  = event.getStarSystem();
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
        sb.append("Is Fuel Star: ");
        sb.append(scoopable);
        sb.append(". ");

        SystemSession.getInstance().setSensorData(sb.toString());
    }
}

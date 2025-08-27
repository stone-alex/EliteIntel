package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.FSDTargetEvent;
import elite.companion.session.SystemSession;

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
            VoiceGenerator.getInstance().speak("Clearing signals data, and Jumping to "+ fsd_target);
        }


        String jumpingTo = event.getName();
        String starClass = event.getStarClass();
        boolean isFuelStar = "KGBFOAM".contains(starClass);

        systemSession.updateSession(SystemSession.FSD_TARGET,jumpingTo);
        //systemSession.setSensorData("Next Jump Target Set to Star System: "+jumpingTo+", Star Class: "+starClass+", Fuel Star: "+isFuelStar);
    }
}

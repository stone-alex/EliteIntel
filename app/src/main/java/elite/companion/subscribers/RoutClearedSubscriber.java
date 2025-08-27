package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.NavRouteClearEvent;
import elite.companion.session.SystemSession;

public class RoutClearedSubscriber {

    public RoutClearedSubscriber() {
        EventBusManager.register(this);
    }


    @Subscribe
    public void onRouteCleared(NavRouteClearEvent event){
        SystemSession.getInstance().clearRoute();
        VoiceGenerator.getInstance().speak("Route cleared");
    }

}

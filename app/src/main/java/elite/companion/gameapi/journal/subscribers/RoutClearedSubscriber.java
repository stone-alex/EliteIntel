package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.journal.events.NavRouteClearEvent;
import elite.companion.session.SystemSession;

@SuppressWarnings("unused")
public class RoutClearedSubscriber {

    @Subscribe
    public void onRouteCleared(NavRouteClearEvent event) {
        SystemSession.getInstance().clearRoute();
        VoiceGenerator.getInstance().speak("Route cleared");
    }

}

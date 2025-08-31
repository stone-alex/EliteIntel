package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.journal.events.ShutdownEvent;
import elite.companion.session.PlayerSession;
import elite.companion.session.SystemSession;

public class ShutDownEventSubscriber {

    @Subscribe
    public void onShutDownEvent(ShutdownEvent event) {
        PlayerSession.getInstance().clearOnShutDown();
        SystemSession.getInstance().clearOnShutDown();
        VoiceGenerator.getInstance().speak("Session off line...");
    }
}

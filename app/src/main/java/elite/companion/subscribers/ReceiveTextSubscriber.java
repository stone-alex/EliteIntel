package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceNotifier;
import elite.companion.events.ReceiveTextEvent;

public class ReceiveTextSubscriber {

    public ReceiveTextSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onReceiveTextEvent(ReceiveTextEvent event) {
        //if(!event.getFrom().isEmpty()){
            VoiceNotifier.getInstance().speakInRandomVoice(event.getMessageLocalised());
        //}
    }
}

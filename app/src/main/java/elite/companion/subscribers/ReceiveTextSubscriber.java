package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.ReceiveTextEvent;

public class ReceiveTextSubscriber {

    public ReceiveTextSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onReceiveTextEvent(ReceiveTextEvent event) {
        //if(!event.getFrom().isEmpty()){
            VoiceGenerator.getInstance().speakInRandomVoice(event.getMessageLocalised());
        //}
    }
}

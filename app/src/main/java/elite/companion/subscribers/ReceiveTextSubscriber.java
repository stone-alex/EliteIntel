package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.ReceiveTextEvent;
import elite.companion.session.SystemSession;

public class ReceiveTextSubscriber {

    public ReceiveTextSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onReceiveTextEvent(ReceiveTextEvent event) {
        if(!event.getMessageLocalised().toLowerCase().contains("entered channel")) {
            boolean isStation = event.getMessage().toLowerCase().contains("station");
            VoiceGenerator.getInstance().speakInRandomVoice(event.getMessageLocalised());
            SystemSession.getInstance().setSensorData(
                    "radio_transmission:[from:" + event.getFrom()+ ", " +
                            "is_station:"+isStation+", " +
                            "message:" + event.getMessageLocalised()
                            +"]"
            );
        }
    }
}

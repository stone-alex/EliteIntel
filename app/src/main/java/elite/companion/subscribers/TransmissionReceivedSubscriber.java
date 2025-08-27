package elite.companion.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.ReceiveTextEvent;
import elite.companion.session.SystemSession;

public class TransmissionReceivedSubscriber {

    public TransmissionReceivedSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onReceiveTextEvent(ReceiveTextEvent event) {
        if (!event.getMessageLocalised().toLowerCase().contains("entered channel")) {
            boolean isStation = event.getMessage().toLowerCase().contains("station");
            if (!event.getFrom().toLowerCase().contains("cruise")) {
                VoiceGenerator.getInstance().speakInRandomVoice(event.getMessageLocalised());
            }

            if (isStation) {
                if (!event.getMessageLocalised().toLowerCase().contains("fire zone")) {
                    SystemSession.getInstance().setSensorData(
                            "radio_transmission:[from:" + event.getFrom() + ", message:" + event.getMessageLocalised() + "]"
                    );
                }
            }
        }
    }
}

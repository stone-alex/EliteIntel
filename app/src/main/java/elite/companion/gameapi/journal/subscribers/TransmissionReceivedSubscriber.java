package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.EventBusManager;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.journal.events.ReceiveTextEvent;
import elite.companion.session.SystemSession;

public class TransmissionReceivedSubscriber {

    public TransmissionReceivedSubscriber() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void onReceiveTextEvent(ReceiveTextEvent event) {
        Object radioOnOffObject= SystemSession.getInstance().getObject(SystemSession.RADION_TRANSMISSION_ON_OFF);
        boolean isRadioOn= radioOnOffObject != null && (boolean) radioOnOffObject;
        if(!isRadioOn) return;


        if (!event.getMessageLocalised().toLowerCase().contains("entered channel")) {
            boolean isStation = event.getMessage().toLowerCase().contains("station");

            if (event.getFrom().toLowerCase().contains("cruise")) return;
            if (event.getFrom().toLowerCase().contains("military")) return;

            SystemSession systemSession = SystemSession.getInstance();

            if (isStation) {
                if (!event.getMessageLocalised().toLowerCase().contains("fire zone")) {
                    systemSession.setSensorData(
                            "radio_transmission:[from:" + event.getFrom() + ", message:" + event.getMessageLocalised() + "]"
                    );
                }
            } else {
                VoiceGenerator.getInstance().speakInRandomVoice(event.getMessageLocalised());
            }
        }
    }
}

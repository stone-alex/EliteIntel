package elite.companion.gameapi.journal.subscribers;

import com.google.common.eventbus.Subscribe;
import elite.companion.gameapi.SensorDataEvent;
import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.gameapi.journal.events.ReceiveTextEvent;
import elite.companion.session.SystemSession;
import elite.companion.util.EventBusManager;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class TransmissionReceivedSubscriber {

    @Subscribe
    public void onReceiveTextEvent(ReceiveTextEvent event) {
        Object radioOnOffObject = SystemSession.getInstance().get(SystemSession.RADION_TRANSMISSION_ON_OFF);
        boolean isRadioOn = radioOnOffObject != null && (boolean) radioOnOffObject;
        if (!isRadioOn) return;

        if (isPirateMessage(event.getMessageLocalised())) {
            EventBusManager.publish(new VoiceProcessEvent("Pirate Alert!!!"));
            return;
        }

        if (!event.getMessageLocalised().toLowerCase().contains("entered channel")) {
            boolean isStation = event.getMessage().toLowerCase().contains("station");

            if (event.getFrom().toLowerCase().contains("cruise")) return;
            if (event.getFrom().toLowerCase().contains("military")) return;

            SystemSession systemSession = SystemSession.getInstance();

            if (isStation) {
                if (!event.getMessageLocalised().toLowerCase().contains("fire zone")) {
                    EventBusManager.publish(new SensorDataEvent("radio_transmission:[from:" + event.getFrom() + ", message:" + event.getMessageLocalised() + "]"));
                }
            } else {
                EventBusManager.publish(new VoiceProcessEvent(event.getMessageLocalised(), true));
            }
        }
    }

    private boolean isPirateMessage(String message) {
        Set<String> pirateTransmissions = new HashSet<>();
        pirateTransmissions.add("Big haul like that, surprised you made it this far".toLowerCase());
        pirateTransmissions.add("Carrying anything nice?".toLowerCase());
        pirateTransmissions.add("Do you have anything of value?".toLowerCase());
        pirateTransmissions.add("I hope you have something good in your hold.".toLowerCase());
        pirateTransmissions.add("I'll pick your bones clean, greenhorn.".toLowerCase());
        pirateTransmissions.add("I see all!".toLowerCase());
        pirateTransmissions.add("I've found my next target and it's you, Commander.".toLowerCase());
        pirateTransmissions.add("Let's see what you are carrying.".toLowerCase());
        pirateTransmissions.add("Let me see what you have.".toLowerCase());
        pirateTransmissions.add("The scan will soon be over.".toLowerCase());
        pirateTransmissions.add("What are you hauling?".toLowerCase());
        pirateTransmissions.add("What do you carry, I wonder?".toLowerCase());
        pirateTransmissions.add("What treats do you carry?".toLowerCase());
        pirateTransmissions.add("What do you have in your cargo hold?".toLowerCase());
        pirateTransmissions.add("Next time you should fill your hold with gold.".toLowerCase());

        return pirateTransmissions.contains(message.toLowerCase());
    }
}

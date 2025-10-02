package elite.intel.ai.mouth.subscribers.events;

public class RadioTransmissionEvent extends BaseVoxEvent{

    public RadioTransmissionEvent(String textToVoice) {
        super(textToVoice, true);
    }
}

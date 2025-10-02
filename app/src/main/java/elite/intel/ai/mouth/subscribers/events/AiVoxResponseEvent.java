package elite.intel.ai.mouth.subscribers.events;

public class AiVoxResponseEvent extends BaseVoxEvent {

    public AiVoxResponseEvent(String textToVoice) {
        super(textToVoice, false);
    }
}

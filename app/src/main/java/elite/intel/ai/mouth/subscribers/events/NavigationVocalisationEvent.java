package elite.intel.ai.mouth.subscribers.events;

public class NavigationVocalisationEvent extends BaseVoxEvent {

    public NavigationVocalisationEvent(String textToVoice) {
        super(textToVoice, false);
    }
}

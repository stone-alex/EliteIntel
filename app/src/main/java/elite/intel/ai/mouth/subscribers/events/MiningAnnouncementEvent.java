package elite.intel.ai.mouth.subscribers.events;

public class MiningAnnouncementEvent extends BaseVoxEvent {

    public MiningAnnouncementEvent(String textToVoice) {
        super(textToVoice, false);
    }
}

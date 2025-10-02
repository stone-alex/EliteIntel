package elite.intel.ai.mouth.subscribers.events;

public class DiscoveryAnnouncementEvent extends BaseVoxEvent {

    public DiscoveryAnnouncementEvent(String textToVoice) {
        super(textToVoice, false);
    }
}

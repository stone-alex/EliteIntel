package elite.intel.ai.mouth.subscribers.events;

public class RadarContactAnnouncementEvent extends BaseVoxEvent {

    public RadarContactAnnouncementEvent(String textToVoice) {
        super(textToVoice, false);
    }
}

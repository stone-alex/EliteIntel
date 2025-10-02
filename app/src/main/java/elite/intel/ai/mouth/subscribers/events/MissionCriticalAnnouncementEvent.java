package elite.intel.ai.mouth.subscribers.events;

public class MissionCriticalAnnouncementEvent extends BaseVoxEvent{

    public MissionCriticalAnnouncementEvent(String textToVoice) {
        super(textToVoice, true);
    }
}

package elite.intel.ai.mouth.subscribers.events;

public class RouteAnnouncementEvent extends BaseVoxEvent {

    public RouteAnnouncementEvent(String textToVoice) {
        super(textToVoice, false);
    }
}

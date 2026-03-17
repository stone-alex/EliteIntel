package elite.intel.ui.event;

public class NotificationVolumeChangedEvent {

    private final float volume;

    public NotificationVolumeChangedEvent(float speed) {
        this.volume = speed;
    }

    public float getVolume() {
        return volume;
    }
}

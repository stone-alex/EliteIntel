package elite.intel.ui.event;

public class SttVolumeChangedEvent {

    private int volume;

    public SttVolumeChangedEvent(int volume) {
        this.volume = volume;
    }

    public int getVolume() {
        return volume;
    }
}

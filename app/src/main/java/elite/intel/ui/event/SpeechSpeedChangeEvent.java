package elite.intel.ui.event;

public class SpeechSpeedChangeEvent {

    private final float speed;

    public SpeechSpeedChangeEvent(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }
}

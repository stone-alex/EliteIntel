package elite.intel.ai.mouth.subscribers.events;

public class TTSInterruptEvent {

    private boolean hasAiReference;

    public TTSInterruptEvent() {
        this(false);
    }
    public TTSInterruptEvent(boolean hasAiReference) {
        this.hasAiReference = hasAiReference;
    }

    public boolean hasAiReference() {
        return this.hasAiReference;
    }
}

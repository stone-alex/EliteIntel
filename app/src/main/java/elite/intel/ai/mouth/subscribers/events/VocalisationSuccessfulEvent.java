package elite.intel.ai.mouth.subscribers.events;

public class VocalisationSuccessfulEvent<T extends BaseVoxEvent> {
    private final T originalRequest;

    public VocalisationSuccessfulEvent(T originalRequest) {
        this.originalRequest = originalRequest;
    }

    public T getOriginalRequest() {
        return originalRequest;
    }

    public String getTextToVoice() {
        return originalRequest.getText();
    }
}
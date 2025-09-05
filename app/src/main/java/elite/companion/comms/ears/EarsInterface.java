package elite.companion.comms.ears;

public interface EarsInterface {
    void stop();
    void start();
    String getNextTranscription() throws InterruptedException;
    void stopListening();
    void shutdown();
}

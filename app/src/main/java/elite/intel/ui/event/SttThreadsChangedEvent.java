package elite.intel.ui.event;

public class SttThreadsChangedEvent {

    private int numThreads;

    public SttThreadsChangedEvent(int numThreads) {
        this.numThreads = numThreads;
    }

    public int getNumThreads() {
        return numThreads;
    }


}

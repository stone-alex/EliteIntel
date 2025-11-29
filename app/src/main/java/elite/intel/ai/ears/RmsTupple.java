package elite.intel.ai.ears;

public class RmsTupple<K, V> {
    private final K rmsHigh;
    private final V rmsLow;

    public RmsTupple(K rmsHigh, V rmsLow) {
        this.rmsHigh = rmsHigh;
        this.rmsLow = rmsLow;
    }

    public K getRmsHigh() {
        return rmsHigh;
    }

    public V getRmsLow() {
        return rmsLow;
    }
}

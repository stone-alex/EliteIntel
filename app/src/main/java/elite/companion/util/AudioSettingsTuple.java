package elite.companion.util;

public class AudioSettingsTuple<K, V> {
    private final K sampleRate;
    private final V bufferSize;

    public AudioSettingsTuple(K sampleRate, V bufferSize) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
    }

    public K getSampleRate() {
        return sampleRate;
    }

    public V getBufferSize() {
        return bufferSize;
    }

    @Override
    public String toString() {
        return "(" + sampleRate + ", " + bufferSize + ")";
    }
}
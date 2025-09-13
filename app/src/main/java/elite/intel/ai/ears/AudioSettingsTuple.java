package elite.intel.ai.ears;

/**
 * A generic class representing a pair of audio settings, typically used for
 * storing and managing configurations such as sample rate and buffer size.
 *
 * @param <K> the type of the sample rate value
 * @param <V> the type of the buffer size value
 */
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
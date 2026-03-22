package elite.intel.ai.ears;

/**
 * Published by WhisperSTTImpl on every captured audio frame (~100ms).
 * Consumed by AudioWaveformPanel via AudioMonitorBus (async, non-blocking).
 */
public class AudioMonitorEvent {

    private final byte[] buffer;
    private final int length;
    private final double rms;
    private final double noiseFloor;
    private final double rmsHigh;

    public AudioMonitorEvent(byte[] buffer, int length, double rms, double noiseFloor, double rmsHigh) {
        this.buffer = buffer;
        this.length = length;
        this.rms = rms;
        this.noiseFloor = noiseFloor;
        this.rmsHigh = rmsHigh;
    }

    /**
     * Raw 16-bit LE PCM bytes at 16 kHz mono.
     */
    public byte[] getBuffer() {
        return buffer;
    }

    public int getLength() {
        return length;
    }

    public double getRms() {
        return rms;
    }

    public double getNoiseFloor() {
        return noiseFloor;
    }

    public double getRmsHigh() {
        return rmsHigh;
    }
}

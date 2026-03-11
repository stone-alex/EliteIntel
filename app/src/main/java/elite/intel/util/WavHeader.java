package elite.intel.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple WAVE file header generator.
 * WAV format is little-endian throughout.
 */
public class WavHeader {
    public static final byte[] RIFF = {'R', 'I', 'F', 'F'};
    public static final byte[] WAVE = {'W', 'A', 'V', 'E'};
    public static final byte[] FMT = {'f', 'm', 't', ' '};
    public static final byte[] DATA = {'d', 'a', 't', 'a'};

    private static final short CHANNELS = 1; // Mono
    private static final short PCM_FORMAT = 1; // PCM

    private final int sampleRate;
    private final short bitsPerSample;
    private final int audioDataBytes;

    /**
     * @param sampleRate     e.g. 16000
     * @param bitsPerSample  e.g. 16
     * @param audioDataBytes exact byte length of the raw PCM data that follows this header
     */
    public WavHeader(int sampleRate, short bitsPerSample, int audioDataBytes) {
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
        this.audioDataBytes = audioDataBytes;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(44);
        try {
            short blockAlign = (short) (CHANNELS * bitsPerSample / 8);
            int byteRate = sampleRate * CHANNELS * bitsPerSample / 8;

            // RIFF chunk
            baos.write(RIFF);
            writeIntLE(baos, 36 + audioDataBytes); // overall file size minus 8 bytes
            baos.write(WAVE);

            // fmt subchunk
            baos.write(FMT);
            writeIntLE(baos, 16);              // subchunk size (always 16 for PCM)
            writeShortLE(baos, PCM_FORMAT);    // audio format: 1 = PCM
            writeShortLE(baos, CHANNELS);      // num channels
            writeIntLE(baos, sampleRate);      // sample rate
            writeIntLE(baos, byteRate);        // byte rate
            writeShortLE(baos, blockAlign);    // block align
            writeShortLE(baos, bitsPerSample); // bits per sample

            // data subchunk
            baos.write(DATA);
            writeIntLE(baos, audioDataBytes);  // actual PCM data size

        } catch (IOException e) {
            throw new RuntimeException("Failed to build WAV header: ", e);
        }
        return baos.toByteArray();
    }

    /**
     * Write a 32-bit int in little-endian byte order.
     */
    private void writeIntLE(OutputStream out, int value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 24) & 0xFF);
    }

    /**
     * Write a 16-bit short in little-endian byte order.
     */
    private void writeShortLE(OutputStream out, short value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }
}
package elite.intel.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Simple WAVE file header generator.
 */
public class WavHeader {
    public static final byte[] RIFF = {'R', 'I', 'F', 'F'};
    public static final byte[] WAVE = {'W', 'A', 'V', 'E'};
    public static final byte[] FMT = "fmt ".getBytes();
    public static final byte[] DATA = "data".getBytes();
    private static final int CHANNELS = 1; // Mono
    private final int sampleRate;
    private final short bitsPerSample;
    private long fileSize;

    public WavHeader(int sampleRate, short bitsPerSample) {
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
    }

    public byte[] toByteArray() {
        // Calculate size
        int headerSize = 44; // Fixed WAV file header size

        ByteArrayOutputStream baos = new ByteArrayOutputStream(headerSize);

        try {
            // File header (RIFF)
            writeString(baos, RIFF);

            // Write chunk size
            fileSize = headerSize - 8;
            byte[] fileSizeBytes = intToByteArray((int) fileSize);
            for (byte b : fileSizeBytes) baos.write(b);

            // Subchunk1 (fmt)
            writeString(baos, FMT);
            writeInt(baos, 16);
            writeShort(baos, (short) 1);
            writeInt(baos, sampleRate);
            writeInt(baos, calculateByteRate());
            writeShort(baos, (short) (bitsPerSample / 8));
            writeShort(baos, (short) bitsPerSample);

            // Data chunk header
            writeString(baos, DATA);
            fileSizeBytes = intToByteArray((int) (audioDataSize()));
            for (byte b : fileSizeBytes) baos.write(b);

        } catch (IOException e) {
            throw new RuntimeException("Failed to build WAV header: ", e);
        }

        return baos.toByteArray();
    }

    private void writeString(OutputStream out, byte[] data) throws IOException {
        for (byte b : data) {
            out.write(b);
        }
    }

    private void writeInt(OutputStream out, int value) throws IOException {
        out.write((value >> 24) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private void writeShort(OutputStream out, short value) throws IOException {
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

    private int calculateByteRate() {
        // Byte rate = sample rate * number of channels * bits per second / 8
        return sampleRate * CHANNELS * bitsPerSample / 8;
    }

    private int audioDataSize() {
        // Placeholder - replace with actual data size if needed
        return 1024;
    }
}
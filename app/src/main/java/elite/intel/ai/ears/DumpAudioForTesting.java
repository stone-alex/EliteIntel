package elite.intel.ai.ears;

import elite.intel.util.WavHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;

public class DumpAudioForTesting {

    private static final Logger log = LogManager.getLogger(DumpAudioForTesting.class);

    private static final DumpAudioForTesting INSTANCE;

    static {
        try {
            INSTANCE = new DumpAudioForTesting();
        } catch (Exception e) {
            throw new RuntimeException("Singleton instance failed to initialize", e);
        }
    }

    private DumpAudioForTesting() {
    }

    public static DumpAudioForTesting getInstance() {
        return INSTANCE;
    }

    public void dumpAudioAsWav(byte[] audio, int sampleRateHertz) {
        if (true) return; // dbug
        String filename = "MicOutputForTesting_" + System.currentTimeMillis() + ".wav";
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            WavHeader header = new WavHeader(sampleRateHertz, (short) 16, audio.length);
            fos.write(header.toByteArray());
            fos.write(audio);
            log.info("Dumped {} bytes of audio to {}", audio.length, filename);
        } catch (IOException e) {
            log.error("Failed to dump audio: ", e);
        }
    }
}
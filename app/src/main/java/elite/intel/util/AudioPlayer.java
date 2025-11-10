package elite.intel.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import javax.sound.sampled.*;
import java.io.*;

public final class AudioPlayer {

    public static final String BEEP_1 = "/beep1.wav";
    public static final String BEEP_2 = "/beep1.wav";

    private static final Logger log = LogManager.getLogger(AudioPlayer.class);
    private static AudioPlayer instance;

    private AudioPlayer() {
    }

    public static AudioPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPlayer();
        }
        return instance;
    }

    public void playBeep(String soundFile) {
        try {
            InputStream resourceStream = getClass().getResourceAsStream(soundFile);
            if (resourceStream == null) {
                log.error("Resource 'beep.wav' not found in classpath");
                return;
            }
            InputStream audioStream = new BufferedInputStream(resourceStream);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    log.info("Finished playing beep.wav");
                }
            });
        } catch (UnsupportedAudioFileException e) {
            log.error("Unsupported audio file format for beep.wav", e);
        } catch (IOException e) {
            log.error("IO error while loading beep.wav", e);
        } catch (LineUnavailableException e) {
            log.error("Audio line unavailable for beep.wav", e);
        } catch (Exception e) {
            log.error("Unexpected error while playing beep.wav", e);
        }
    }
}
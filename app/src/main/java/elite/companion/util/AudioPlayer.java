package elite.companion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.*;

public final class AudioPlayer {

    private static final Logger log = LoggerFactory.getLogger(AudioPlayer.class);
    private static AudioPlayer instance;

    private AudioPlayer() {
    }

    public static AudioPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPlayer();
        }
        return instance;
    }
    public void playBeep() {
        try {
            // Load resource from classpath
            InputStream resourceStream = getClass().getResourceAsStream("/beep.wav");
            if (resourceStream == null) {
                log.error("Resource 'beep.wav' not found in classpath");
                return;
            }
            // Wrap in BufferedInputStream to support mark/reset
            InputStream audioStream = new BufferedInputStream(resourceStream);
            log.info("Successfully loaded beep.wav resource");
            // Create an AudioInputStream
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioStream);
            log.info("Created AudioInputStream for beep.wav");
            // Get a Clip to play the audio
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            log.info("Opened clip for beep.wav");
            clip.start();
            log.info("Started playing beep.wav");
            // Ensure clip closes after playback
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

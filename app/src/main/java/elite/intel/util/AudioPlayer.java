package elite.intel.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import javax.sound.sampled.*;
import java.io.*;

public final class AudioPlayer {

    public static final String BEEP_1 = "/beep1.wav";
    public static final String BEEP_2 = "/beep2.wav";
    public static final String BEEP_3 = "/beep3.wav";

    private static final Logger log = LogManager.getLogger(AudioPlayer.class);
    private static AudioPlayer instance;
    private static float volume = 0.8f;

    private AudioPlayer() {
    }

    public static AudioPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPlayer();
        }
        return instance;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (volume < 0.0f || volume > 1.0f) {
            throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
        }
        AudioPlayer.volume = volume;
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
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);
            }
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
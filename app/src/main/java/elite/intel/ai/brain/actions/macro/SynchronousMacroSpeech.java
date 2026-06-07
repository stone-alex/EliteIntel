package elite.intel.ai.brain.actions.macro;

import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.ai.ears.IsSpeakingEvent;
import elite.intel.gameapi.EventBusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Default {@link MacroSpeakExecutor}: publishes {@link AiVoxResponseEvent} with a
 * {@link CompletableFuture} completion signal and blocks until the TTS pipeline completes
 * playback of the last sentence, or until the 30-second guard timeout elapses.
 * <p>
 * The TTS implementation (Google or Kokoro) is responsible for completing the future after
 * the last sentence of this request finishes playing. On timeout the macro logs a warning
 * and continues to the next step.
 * <p>
 * Publishes {@link IsSpeakingEvent}{@code (true)} before speech begins and
 * {@link IsSpeakingEvent}{@code (false)} in a {@code finally} block so that STT is
 * suppressed for the full duration of playback, even if an exception or timeout occurs.
 */
class SynchronousMacroSpeech implements MacroSpeakExecutor {

    static final MacroSpeakExecutor DEFAULT = new SynchronousMacroSpeech();

    private static final Logger log = LogManager.getLogger(SynchronousMacroSpeech.class);
    private static final int TIMEOUT_SECONDS = 30;

    private SynchronousMacroSpeech() {}

    @Override
    public void speak(String text) throws InterruptedException {
        CompletableFuture<Void> done = new CompletableFuture<>();
        EventBusManager.publish(new IsSpeakingEvent(true));
        try {
            EventBusManager.publish(new AiVoxResponseEvent(text, done));
            done.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("Macro SPEAK timed out after {}s for: '{}'", TIMEOUT_SECONDS, text);
        } catch (ExecutionException e) {
            log.warn("Macro SPEAK completed exceptionally: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } finally {
            EventBusManager.publish(new IsSpeakingEvent(false));
        }
        // InterruptedException propagates to CustomCommandHandler, which will interrupt macro execution
    }
}

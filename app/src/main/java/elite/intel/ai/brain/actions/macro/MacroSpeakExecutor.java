package elite.intel.ai.brain.actions.macro;

/**
 * Strategy for executing a SPEAK step in a macro.
 * <p>
 * The implementation must block the calling thread until speech playback is complete
 * (or until a timeout or failure), so the next macro step does not start while TTS is still speaking.
 *
 * @see SynchronousMacroSpeech
 */
@FunctionalInterface
interface MacroSpeakExecutor {

    /**
     * Speaks {@code text} and blocks until TTS playback finishes.
     *
     * @throws InterruptedException if the macro execution thread is interrupted while waiting
     */
    void speak(String text) throws InterruptedException;
}

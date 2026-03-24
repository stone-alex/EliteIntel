package elite.intel.ai.ears;

/** Selects the local STT backend. Provider change takes effect on next app restart. */
public enum SttProvider {
    WHISPER,
    NEMO_PARAKEET
}

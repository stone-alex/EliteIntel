package elite.intel.ai.hands;

/**
 * Platform-specific keyboard input for left/right modifier key differentiation.
 * java.awt.Robot cannot distinguish VK_LCONTROL from VK_RCONTROL; this abstraction
 * routes those keys through native APIs that can.
 */
public interface NativeKeyInput {
    /**
     * Generate a key-down event for the given KeyProcessor synthetic code.
     *
     * @param syntheticCode one of the KEY_LEFT or KEY_RIGHT modifier constants from KeyProcessor
     *                      that are >= KeyProcessor.NATIVE_BASE
     */
    void keyDown(int syntheticCode);

    /**
     * Generate a key-up event for the given KeyProcessor synthetic code.
     *
     * @param syntheticCode one of the KEY_LEFT or KEY_RIGHT modifier constants from KeyProcessor
     *                      that are >= KeyProcessor.NATIVE_BASE
     */
    void keyUp(int syntheticCode);
}

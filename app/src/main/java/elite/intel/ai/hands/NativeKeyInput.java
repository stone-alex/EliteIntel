package elite.intel.ai.hands;

/**
 * Platform-specific keyboard input that sends hardware scan-code events understood
 * by DirectInput. java.awt.Robot sends VK-code events which DirectInput may ignore.
 */
public interface NativeKeyInput {
    void keyDown(int keyCode);

    void keyUp(int keyCode);

    /**
     * Returns true if this implementation has a mapping for the given key code and
     * will handle it natively. When false, KeyProcessor falls back to java.awt.Robot.
     */
    default boolean handles(int keyCode) {
        return false;
    }

    /**
     * Types a single Unicode character correctly for the active keyboard layout.
     * Returns true if the character was sent; false if the caller should fall back to Robot.
     * Used by KeyProcessor.enterText() to handle non-QWERTY layouts (e.g. AZERTY) where
     * Robot.keyPress(VK_5) presses the physical position-5 key without knowing whether
     * Shift is needed to produce the '5' character.
     */
    default boolean typeChar(char c) {
        return false;
    }
}

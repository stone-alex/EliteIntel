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
}

package elite.intel.ai.hands;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Cross-platform left/right modifier key simulator.
 * <p>
 * Java's Robot API cannot distinguish left vs right modifier keys - keyPress(VK_CONTROL)
 * always sends the generic (left) variant. This class calls platform-native APIs directly:
 * Windows: user32.dll keybd_event with VK_LCONTROL/VK_RCONTROL/VK_LMENU/VK_RMENU
 * Linux:   libXtst XTestFakeKeyEvent with XK_Control_L/R, XK_Alt_L/R keysyms
 * <p>
 * Falls back to Robot's generic VK codes if JNA initialisation fails.
 */
public class ModifierKeySimulator {
    private static final Logger log = LogManager.getLogger(ModifierKeySimulator.class);

    private static final boolean IS_WINDOWS =
            System.getProperty("os.name", "").toLowerCase().contains("win");

    // Stable modifier IDs used as map keys (values are arbitrary, just unique)
    public static final int LEFT_CTRL = 1;
    public static final int RIGHT_CTRL = 2;
    public static final int LEFT_ALT = 3;
    public static final int RIGHT_ALT = 4;
    public static final int LEFT_SHIFT = 5;
    public static final int RIGHT_SHIFT = 6;

    // ── Windows ────────────────────────────────────────────────────────────────

    /**
     * Windows VK codes that map to specific left/right modifier keys.
     */
    private static final Map<Integer, Byte> WIN_VK = new HashMap<>();

    static {
        WIN_VK.put(LEFT_SHIFT, (byte) 0xA0); // VK_LSHIFT
        WIN_VK.put(RIGHT_SHIFT, (byte) 0xA1); // VK_RSHIFT
        WIN_VK.put(LEFT_CTRL, (byte) 0xA2); // VK_LCONTROL
        WIN_VK.put(RIGHT_CTRL, (byte) 0xA3); // VK_RCONTROL
        WIN_VK.put(LEFT_ALT, (byte) 0xA4); // VK_LMENU
        WIN_VK.put(RIGHT_ALT, (byte) 0xA5); // VK_RMENU
    }

    private static final int KEYEVENTF_KEYUP = 0x0002;

    interface WinUser32 extends Library {
        void keybd_event(byte bVk, byte bScan, int dwFlags, Pointer dwExtraInfo);
    }

    // ── Linux / X11 ────────────────────────────────────────────────────────────

    /**
     * X11 keysyms for each left/right modifier.
     */
    private static final Map<Integer, Long> LINUX_KEYSYM = new HashMap<>();

    static {
        LINUX_KEYSYM.put(LEFT_SHIFT, 0xFFE1L); // XK_Shift_L
        LINUX_KEYSYM.put(RIGHT_SHIFT, 0xFFE2L); // XK_Shift_R
        LINUX_KEYSYM.put(LEFT_CTRL, 0xFFE3L); // XK_Control_L
        LINUX_KEYSYM.put(RIGHT_CTRL, 0xFFE4L); // XK_Control_R
        LINUX_KEYSYM.put(LEFT_ALT, 0xFFE9L); // XK_Alt_L
        LINUX_KEYSYM.put(RIGHT_ALT, 0xFFEAL); // XK_Alt_R
    }

    interface X11Lib extends Library {
        long XOpenDisplay(String displayName);   // null = $DISPLAY

        int XKeysymToKeycode(long display, long keysym);

        int XFlush(long display);
    }

    interface XtstLib extends Library {
        int XTestFakeKeyEvent(long display, int keycode, boolean isPress, long delay);
    }

    // ── Generic fallback VK codes (Robot) ──────────────────────────────────────

    private static final Map<Integer, Integer> FALLBACK_VK = new HashMap<>();

    static {
        FALLBACK_VK.put(LEFT_CTRL, KeyEvent.VK_CONTROL);
        FALLBACK_VK.put(RIGHT_CTRL, KeyEvent.VK_CONTROL);
        FALLBACK_VK.put(LEFT_ALT, KeyEvent.VK_ALT);
        FALLBACK_VK.put(RIGHT_ALT, KeyEvent.VK_ALT);
        FALLBACK_VK.put(LEFT_SHIFT, KeyEvent.VK_SHIFT);
        FALLBACK_VK.put(RIGHT_SHIFT, KeyEvent.VK_SHIFT);
    }

    // ── State ──────────────────────────────────────────────────────────────────

    private boolean available = false;
    private WinUser32 winUser32;
    private X11Lib x11;
    private XtstLib xtst;
    private long x11Display;

    public ModifierKeySimulator() {
        try {
            if (IS_WINDOWS) {
                winUser32 = Native.load("user32", WinUser32.class);
                available = true;
                log.info("ModifierKeySimulator ready (Windows/user32)");
            } else {
                x11 = Native.load("X11", X11Lib.class);
                xtst = Native.load("Xtst", XtstLib.class);
                x11Display = x11.XOpenDisplay(null);
                if (x11Display == 0) throw new RuntimeException("XOpenDisplay returned null - is DISPLAY set?");
                available = true;
                log.info("ModifierKeySimulator ready (Linux/XTest)");
            }
        } catch (Exception e) {
            log.warn("ModifierKeySimulator unavailable ({}): left/right modifier keys will use generic fallback",
                    e.getMessage());
        }
    }

    /**
     * True when native key simulation is working; false means fallback to Robot VK codes.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Returns the fallback Robot VK code for a modifier ID, used when native
     * simulation is unavailable. Left and right variants both return the generic VK.
     */
    public int fallbackVk(int modifierId) {
        return FALLBACK_VK.getOrDefault(modifierId, KeyEvent.VK_CONTROL);
    }

    /**
     * Press (hold down) the given modifier key.
     */
    public void press(int modifierId) {
        if (!available) return;
        if (IS_WINDOWS) pressWindows(modifierId);
        else pressLinux(modifierId);
    }

    /**
     * Release the given modifier key.
     */
    public void release(int modifierId) {
        if (!available) return;
        if (IS_WINDOWS) releaseWindows(modifierId);
        else releaseLinux(modifierId);
    }

    private void pressWindows(int modifierId) {
        Byte vk = WIN_VK.get(modifierId);
        if (vk == null) return;
        winUser32.keybd_event(vk, (byte) 0, 0, null);
    }

    private void releaseWindows(int modifierId) {
        Byte vk = WIN_VK.get(modifierId);
        if (vk == null) return;
        winUser32.keybd_event(vk, (byte) 0, KEYEVENTF_KEYUP, null);
    }

    private void pressLinux(int modifierId) {
        Long keysym = LINUX_KEYSYM.get(modifierId);
        if (keysym == null) return;
        int keycode = x11.XKeysymToKeycode(x11Display, keysym);
        xtst.XTestFakeKeyEvent(x11Display, keycode, true, 0);
        x11.XFlush(x11Display);
    }

    private void releaseLinux(int modifierId) {
        Long keysym = LINUX_KEYSYM.get(modifierId);
        if (keysym == null) return;
        int keycode = x11.XKeysymToKeycode(x11Display, keysym);
        xtst.XTestFakeKeyEvent(x11Display, keycode, false, 0);
        x11.XFlush(x11Display);
    }
}

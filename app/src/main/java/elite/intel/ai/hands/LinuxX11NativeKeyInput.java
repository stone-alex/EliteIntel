package elite.intel.ai.hands;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.X11;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static elite.intel.ai.hands.KeyProcessor.NATIVE_BASE;

/**
 * Linux X11 implementation of NativeKeyInput using libX11 + libXtst (XTest extension).
 * Uses XTestFakeKeyEvent to inject hardware-level key events that correctly distinguish
 * left/right modifier keysyms (XK_Control_L vs XK_Control_R, etc.).
 * <p>
 * Falls back gracefully if X11 display is unavailable (pure Wayland without XWayland).
 * In that case isAvailable() returns false and the factory will use the Robot fallback.
 */
class LinuxX11NativeKeyInput implements NativeKeyInput {
    private static final Logger log = LogManager.getLogger(LinuxX11NativeKeyInput.class);

    /**
     * Minimal JNA interface for the XTest extension in libXtst.
     * XTestFakeKeyEvent injects a key event directly into the X input stream.
     */
    interface XTst extends Library {
        XTst INSTANCE = Native.load("Xtst", XTst.class);

        /**
         * @param display  the X11 display connection
         * @param keycode  hardware keycode (NOT keysym; use XKeysymToKeycode to convert)
         * @param is_press 1 for press, 0 for release
         * @param delay    event delay in milliseconds (0 = immediate)
         */
        int XTestFakeKeyEvent(X11.Display display, int keycode, int is_press, NativeLong delay);
    }

    // X11 keysyms for left/right modifiers
    // Reference: /usr/include/X11/keysymdef.h
    private static final Map<Integer, Long> KEYSYM_MAP = new HashMap<>();

    static {
        KEYSYM_MAP.put(NATIVE_BASE + 1, 0xFFE3L);  // KEY_LEFTCONTROL  → XK_Control_L
        KEYSYM_MAP.put(NATIVE_BASE + 2, 0xFFE4L);  // KEY_RIGHTCONTROL → XK_Control_R
        KEYSYM_MAP.put(NATIVE_BASE + 3, 0xFFE1L);  // KEY_LEFTSHIFT    → XK_Shift_L
        KEYSYM_MAP.put(NATIVE_BASE + 4, 0xFFE2L);  // KEY_RIGHTSHIFT   → XK_Shift_R
        KEYSYM_MAP.put(NATIVE_BASE + 5, 0xFFE9L);  // KEY_LEFTALT      → XK_Alt_L
        KEYSYM_MAP.put(NATIVE_BASE + 6, 0xFFEAL);  // KEY_RIGHTALT     → XK_Alt_R
        KEYSYM_MAP.put(NATIVE_BASE + 7, 0xFFEBL);  // KEY_LEFTSUPER    → XK_Super_L
        KEYSYM_MAP.put(NATIVE_BASE + 8, 0xFFECL);  // KEY_RIGHTSUPER   → XK_Super_R
    }

    private final X11 x11;
    private final XTst xtst;
    private final X11.Display display;
    // Cache keysym → hardware keycode (XKeysymToKeycode is not free; returns byte)
    private final Map<Integer, Byte> keycodeCache = new HashMap<>();
    private final boolean available;

    LinuxX11NativeKeyInput() {
        X11 x11ref = null;
        XTst xtstRef = null;
        X11.Display disp = null;
        boolean ok = false;

        try {
            x11ref = X11.INSTANCE;
            xtstRef = XTst.INSTANCE;
            disp = x11ref.XOpenDisplay(null);
            if (disp == null) {
                log.warn("XOpenDisplay returned null - likely pure Wayland session. " +
                        "Left/right modifier distinction will fall back to generic Robot keys.");
            } else {
                // Pre-populate keycode cache for all known synthetic codes
                for (Map.Entry<Integer, Long> entry : KEYSYM_MAP.entrySet()) {
                    byte kc = x11ref.XKeysymToKeycode(disp, new X11.KeySym(entry.getValue()));
                    if (kc == 0) {
                        log.warn("XKeysymToKeycode returned 0 for keysym 0x{} - key may not be mapped in current X keymap",
                                Long.toHexString(entry.getValue()));
                    }
                    keycodeCache.put(entry.getKey(), kc);
                }
                ok = true;
                log.debug("LinuxX11NativeKeyInput initialised, {} keycodes mapped", keycodeCache.size());
            }
        } catch (UnsatisfiedLinkError e) {
            log.warn("libXtst not available: {}. Left/right modifier distinction unavailable.", e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to initialise X11 native key input: {}", e.getMessage());
        }

        this.x11 = x11ref;
        this.xtst = xtstRef;
        this.display = disp;
        this.available = ok;
    }

    boolean isAvailable() {
        return available;
    }

    @Override
    public void keyDown(int syntheticCode) {
        fakeKeyEvent(syntheticCode, true);
    }

    @Override
    public void keyUp(int syntheticCode) {
        fakeKeyEvent(syntheticCode, false);
    }

    private void fakeKeyEvent(int syntheticCode, boolean press) {
        Byte keycode = keycodeCache.get(syntheticCode);
        if (keycode == null) {
            log.warn("No X11 keycode cached for synthetic code 0x{}", Integer.toHexString(syntheticCode));
            return;
        }
        if (keycode == 0) {
            log.warn("X11 keycode is 0 for synthetic code 0x{} - skipping", Integer.toHexString(syntheticCode));
            return;
        }
        xtst.XTestFakeKeyEvent(display, keycode & 0xFF, press ? 1 : 0, new NativeLong(0));
        x11.XFlush(display);
    }
}

package elite.intel.ai.hands;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static elite.intel.ai.hands.KeyProcessor.NATIVE_BASE;

/**
 * Windows implementation of NativeKeyInput using user32.dll SendInput.
 *
 * Uses KEYEVENTF_SCANCODE with hardcoded PS/2 hardware scan codes rather than VK codes.
 * This is critical for two reasons:
 *  1. DirectInput (used by Elite Dangerous) identifies keys by hardware scan code, not VK code.
 *     Sending wScan=0 with a VK code leaves the scan code field undefined for DirectInput,
 *     causing multi-modifier combos to silently fail even though simple Robot key presses
 *     (which auto-compute scan codes via MapVirtualKey) work fine.
 *  2. Sending VK_RMENU via VK code on AltGr keyboard layouts causes Windows to inject a
 *     synthetic VK_LCONTROL, corrupting the modifier state seen by the game.
 *
 * Extended keys (right-side modifiers, Win, Apps) use KEYEVENTF_EXTENDEDKEY alongside
 * KEYEVENTF_SCANCODE to produce the correct E0-prefixed scan code sequence.
 */
class WindowsNativeKeyInput implements NativeKeyInput {
    private static final Logger log = LogManager.getLogger(WindowsNativeKeyInput.class);

    private static final int KEYEVENTF_KEYUP = 0x0002;
    private static final int KEYEVENTF_EXTENDEDKEY = 0x0001;
    private static final int KEYEVENTF_SCANCODE = 0x0008;

    // Hardware scan codes (PS/2 Set 1). Extended keys need KEYEVENTF_EXTENDEDKEY
    // so the effective scan code is E0-prefixed (e.g. Right Alt = E0 38).
    private static final Map<Integer, Short> SCAN_MAP = new HashMap<>();
    private static final Set<Integer> NEEDS_EXTENDED = Set.of(
            NATIVE_BASE + 2,   // KEY_RIGHTCONTROL → E0 1D
            NATIVE_BASE + 6,   // KEY_RIGHTALT     → E0 38
            NATIVE_BASE + 7,   // KEY_LEFTSUPER    → E0 5B
            NATIVE_BASE + 8,   // KEY_RIGHTSUPER   → E0 5C
            NATIVE_BASE + 9    // KEY_MENU/APPS    → E0 5D
    );

    static {
        SCAN_MAP.put(NATIVE_BASE + 1, (short) 0x1D);  // KEY_LEFTCONTROL  → scan 0x1D
        SCAN_MAP.put(NATIVE_BASE + 2, (short) 0x1D);  // KEY_RIGHTCONTROL → E0 1D
        SCAN_MAP.put(NATIVE_BASE + 3, (short) 0x2A);  // KEY_LEFTSHIFT    → scan 0x2A
        SCAN_MAP.put(NATIVE_BASE + 4, (short) 0x36);  // KEY_RIGHTSHIFT   → scan 0x36
        SCAN_MAP.put(NATIVE_BASE + 5, (short) 0x38);  // KEY_LEFTALT      → scan 0x38
        SCAN_MAP.put(NATIVE_BASE + 6, (short) 0x38);  // KEY_RIGHTALT     → E0 38
        SCAN_MAP.put(NATIVE_BASE + 7, (short) 0x5B);  // KEY_LEFTSUPER    → E0 5B
        SCAN_MAP.put(NATIVE_BASE + 8, (short) 0x5C);  // KEY_RIGHTSUPER   → E0 5C
        SCAN_MAP.put(NATIVE_BASE + 9, (short) 0x5D);  // KEY_MENU/APPS    → E0 5D
    }

    @Override
    public void keyDown(int syntheticCode) {
        sendKeyEvent(syntheticCode, false);
    }

    @Override
    public void keyUp(int syntheticCode) {
        sendKeyEvent(syntheticCode, true);
    }

    private void sendKeyEvent(int syntheticCode, boolean isKeyUp) {
        Short scan = SCAN_MAP.get(syntheticCode);
        if (scan == null) {
            log.warn("No Windows scan code mapping for synthetic code 0x{}", Integer.toHexString(syntheticCode));
            return;
        }

        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.setType(WinUser.KEYBDINPUT.class);
        input.input.ki.wVk = new WinDef.WORD(0);
        input.input.ki.wScan = new WinDef.WORD(scan);

        int flags = KEYEVENTF_SCANCODE;
        if (NEEDS_EXTENDED.contains(syntheticCode)) flags |= KEYEVENTF_EXTENDEDKEY;
        if (isKeyUp) flags |= KEYEVENTF_KEYUP;
        input.input.ki.dwFlags = new WinDef.DWORD(flags);
        input.input.ki.time = new WinDef.DWORD(0);

        WinDef.DWORD sent = User32.INSTANCE.SendInput(
                new WinDef.DWORD(1),
                (WinUser.INPUT[]) input.toArray(1),
                input.size()
        );
        if (sent.intValue() != 1) {
            log.warn("SendInput sent={} for code=0x{}, keyUp={}", sent.intValue(),
                    Integer.toHexString(syntheticCode), isKeyUp);
        }
    }
}

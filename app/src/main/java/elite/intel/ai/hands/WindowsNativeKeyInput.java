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
 * Sends hardware-level virtual key codes that distinguish left/right modifiers:
 * VK_LCONTROL (0xA2), VK_RCONTROL (0xA3), VK_LSHIFT (0xA0), VK_RSHIFT (0xA1),
 * VK_LMENU (0xA4), VK_RMENU (0xA5), VK_LWIN (0x5B), VK_RWIN (0x5C).
 */
class WindowsNativeKeyInput implements NativeKeyInput {
    private static final Logger log = LogManager.getLogger(WindowsNativeKeyInput.class);

    private static final int KEYEVENTF_KEYUP = 0x0002;
    private static final int KEYEVENTF_EXTENDEDKEY = 0x0001;

    // Maps KeyProcessor NATIVE_BASE+N codes to Windows virtual key codes
    private static final Map<Integer, Short> VK_MAP = new HashMap<>();

    // Right-side modifiers that need KEYEVENTF_EXTENDEDKEY for correct scan code
    private static final Set<Short> EXTENDED_VKS;

    static {
        VK_MAP.put(NATIVE_BASE + 1, (short) 0xA2);  // KEY_LEFTCONTROL  → VK_LCONTROL
        VK_MAP.put(NATIVE_BASE + 2, (short) 0xA3);  // KEY_RIGHTCONTROL → VK_RCONTROL
        VK_MAP.put(NATIVE_BASE + 3, (short) 0xA0);  // KEY_LEFTSHIFT    → VK_LSHIFT
        VK_MAP.put(NATIVE_BASE + 4, (short) 0xA1);  // KEY_RIGHTSHIFT   → VK_RSHIFT
        VK_MAP.put(NATIVE_BASE + 5, (short) 0xA4);  // KEY_LEFTALT      → VK_LMENU
        VK_MAP.put(NATIVE_BASE + 6, (short) 0xA5);  // KEY_RIGHTALT     → VK_RMENU
        VK_MAP.put(NATIVE_BASE + 7, (short) 0x5B);  // KEY_LEFTSUPER    → VK_LWIN
        VK_MAP.put(NATIVE_BASE + 8, (short) 0x5C);  // KEY_RIGHTSUPER   → VK_RWIN

        EXTENDED_VKS = Set.of(
                (short) 0xA3,  // VK_RCONTROL
                (short) 0xA5   // VK_RMENU (right alt)
        );
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
        Short vk = VK_MAP.get(syntheticCode);
        if (vk == null) {
            log.warn("No Windows VK mapping for synthetic code 0x{}", Integer.toHexString(syntheticCode));
            return;
        }

        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.setType(WinUser.KEYBDINPUT.class);
        input.input.ki.wVk = new WinDef.WORD(vk);
        input.input.ki.wScan = new WinDef.WORD(0);

        int flags = isKeyUp ? KEYEVENTF_KEYUP : 0;
        if (EXTENDED_VKS.contains(vk)) {
            flags |= KEYEVENTF_EXTENDEDKEY;
        }
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

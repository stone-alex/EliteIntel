package elite.companion.robot;

import java.awt.*;

public class KeyProcessor {

    private final Robot robot;

    public static final int KEY_SPACE = 32;
    public static final int KEY_ENTER = 10;
    public static final int KEY_BACKSPACE = 8;
    public static final int KEY_TAB = 9;
    public static final int KEY_ESCAPE = 27;
    public static final int KEY_UP = 38;
    public static final int KEY_DOWN = 40;
    public static final int KEY_LEFT = 37;
    public static final int KEY_RIGHT = 39;
    public static final int KEY_DELETE = 46;
    public static final int KEY_F1 = 112;
    public static final int KEY_F2 = 113;
    public static final int KEY_F3 = 114;
    public static final int KEY_F4 = 115;
    public static final int KEY_F5 = 116;
    public static final int KEY_F6 = 117;
    public static final int KEY_F7 = 118;
    public static final int KEY_F8 = 119;
    public static final int KEY_F9 = 120;
    public static final int KEY_F10 = 121;
    public static final int KEY_F11 = 122;
    public static final int KEY_F12 = 123;
    public static final int KEY_PRINTSCREEN = 154;
    public static final int KEY_SCROLL_LOCK = 145;
    public static final int KEY_PAUSE = 19;
    public static final int KEY_INSERT = 45;
    public static final int KEY_HOME = 36;
    public static final int KEY_PAGE_UP = 33;
    public static final int KEY_PAGE_DOWN = 34;
    public static final int KEY_END = 35;
    public static final int KEY_CAPS_LOCK = 20;
    public static final int KEY_NUM_LOCK = 144;
    public static final int KEY_NUM_SLASH = 111;
    public static final int KEY_NUM_ASTERISK = 106;
    public static final int KEY_NUM_MINUS = 109;
    public static final int KEY_NUM_PLUS = 107;
    public static final int KEY_NUM_1 = 49;
    public static final int KEY_NUM_2 = 50;
    public static final int KEY_NUM_3 = 51;
    public static final int KEY_NUM_4 = 52;
    public static final int KEY_NUM_5 = 53;
    public static final int KEY_NUM_6 = 54;
    public static final int KEY_NUM_7 = 55;
    public static final int KEY_NUM_8 = 56;
    public static final int KEY_NUM_9 = 57;
    public static final int KEY_NUM_0 = 48;
    public static final int KEY_NUM_PERIOD = 110;
    public static final int KEY_NUM_ENTER = 141;
    public static final int KEY_NUM_EQUALS = 187;
    public static final int KEY_NUM_BACKSPACE = 142;
    public static final int KEY_NUM_TAB = 143;
    public static final int KEY_NUM_CLEAR = 12;
    public static final int KEY_NUM_SLASH_PERIOD = 111;
    public static final int KEY_LEFT_CONTROL = 17;
    public static final int KEY_LEFT_SHIFT = 16;
    public static final int KEY_LEFT_ALT = 18;
    public static final int KEY_LEFT_SUPER = 91;
    public static final int KEY_RIGHT_CONTROL = 17;
    public static final int KEY_RIGHT_SHIFT = 16;
    public static final int KEY_RIGHT_ALT = 18;
    public static final int KEY_RIGHT_SUPER = 91;
    public static final int KEY_MENU = 157;
    public static final int KEY_LEFT_BRACKET = 91;
    public static final int KEY_BACKSLASH = 92;
    public static final int KEY_RIGHT_BRACKET = 93;
    public static final int KEY_GRAVE_ACCENT = 96;

    // Letters A-Z
    public static final int KEY_A = 65;
    public static final int KEY_B = 66;
    public static final int KEY_C = 67;
    public static final int KEY_D = 68;
    public static final int KEY_E = 69;
    public static final int KEY_F = 70;
    public static final int KEY_G = 71;
    public static final int KEY_H = 72;
    public static final int KEY_I = 73;
    public static final int KEY_J = 74;
    public static final int KEY_K = 75;
    public static final int KEY_L = 76;
    public static final int KEY_M = 77;
    public static final int KEY_N = 78;
    public static final int KEY_O = 79;
    public static final int KEY_P = 80;
    public static final int KEY_Q = 81;
    public static final int KEY_R = 82;
    public static final int KEY_S = 83;
    public static final int KEY_T = 84;
    public static final int KEY_U = 85;
    public static final int KEY_V = 86;
    public static final int KEY_W = 87;
    public static final int KEY_X = 88;
    public static final int KEY_Y = 89;
    public static final int KEY_Z = 90;

    // Number row 0-9 (main keyboard)
    public static final int KEY_0 = 48;
    public static final int KEY_1 = 49;
    public static final int KEY_2 = 50;
    public static final int KEY_3 = 51;
    public static final int KEY_4 = 52;
    public static final int KEY_5 = 53;
    public static final int KEY_6 = 54;
    public static final int KEY_7 = 55;
    public static final int KEY_8 = 56;
    public static final int KEY_9 = 57;

    // Punctuation keys on standard US QWERTY
    public static final int KEY_MINUS = 45;        // '-'
    public static final int KEY_EQUALS = 61;       // '='
    public static final int KEY_SEMICOLON = 59;    // ';'
    public static final int KEY_APOSTROPHE = 39;   // '\''
    public static final int KEY_COMMA = 44;        // ','
    public static final int KEY_PERIOD = 46;       // '.'
    public static final int KEY_SLASH = 47;        // '/'
    public static final int KEY_TILDE = 96;        // '`' (same as GRAVE)

    // Symbols requiring Shift (for reference; same base codes as above)
    public static final int KEY_EXCLAMATION = 49;  // '!' -> Shift+1
    public static final int KEY_AT = 50;           // '@' -> Shift+2
    public static final int KEY_HASH = 51;         // '#' -> Shift+3
    public static final int KEY_DOLLAR = 52;       // '$' -> Shift+4
    public static final int KEY_PERCENT = 53;      // '%' -> Shift+5
    public static final int KEY_CARET = 54;        // '^' -> Shift+6
    public static final int KEY_AMPERSAND = 55;    // '&' -> Shift+7
    public static final int KEY_ASTERISK = 56;     // '*' -> Shift+8
    public static final int KEY_LEFT_PAREN = 57;   // '(' -> Shift+9
    public static final int KEY_RIGHT_PAREN = 48;  // ')' -> Shift+0
    public static final int KEY_UNDERSCORE = 45;   // '_' -> Shift+'-'
    public static final int KEY_PLUS = 61;         // '+' -> Shift+'='
    public static final int KEY_COLON = 59;        // ':' -> Shift+';'
    public static final int KEY_QUOTE = 39;        // '"' -> Shift+'\''
    public static final int KEY_LESS = 44;         // '<' -> Shift+','
    public static final int KEY_GREATER = 46;      // '>' -> Shift+'.'
    public static final int KEY_QUESTION = 47;     // '?' -> Shift+'/'
    public static final int KEY_TILDE_SHIFT = 96;  // '~' -> Shift+'`'

    private static KeyProcessor instance;

    public static KeyProcessor getInstance() {
        if (instance == null) {
            try {
                instance = new KeyProcessor();
            } catch (AWTException e) {
                throw new RuntimeException("Failed to initialize KeyboardProcessor", e);
            }
        }
        return instance;
    }

    private KeyProcessor() throws AWTException {
        this.robot = new Robot();
        robot.setAutoDelay(50);
    }

    public void pressKey(int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    public void holdKey(int keyCode) {
        robot.keyPress(keyCode);
    }

    public void releaseKey(int keyCode) {
        robot.keyRelease(keyCode);
    }

    public void pressKeys(int... keyCodes) {
        for (int keyCode : keyCodes) {
            robot.keyPress(keyCode);
        }

        for (int i = keyCodes.length - 1; i >= 0; i--) {
            robot.keyRelease(keyCodes[i]);
        }
    }

    public void pressKeyCombo(int... keyCodes) {
        try {
            for (int keyCode : keyCodes) {
                robot.keyPress(keyCode);
            }
            Thread.sleep(100);
            for (int i = keyCodes.length - 1; i >= 0; i--) {
                robot.keyRelease(keyCodes[i]);
            }
        } catch (InterruptedException e) {
            for (int i = keyCodes.length - 1; i >= 0; i--) {
                robot.keyRelease(keyCodes[i]);
            }
            Thread.currentThread().interrupt();
        }
    }
}

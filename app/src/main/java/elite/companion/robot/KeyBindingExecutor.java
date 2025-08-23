package elite.companion.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class KeyBindingExecutor {
    private static final Logger log = LoggerFactory.getLogger(KeyBindingExecutor.class);

    private final KeyProcessor keyProcessor;
    private static final Map<String, Integer> ELITE_TO_KEYPROCESSOR_MAP = new HashMap<>();

    static {
        try {
            for (Field field : KeyProcessor.class.getDeclaredFields()) {
                if (field.getName().startsWith("KEY_") && field.getType() == int.class) {
                    String eliteKeyName = convertToEliteKeyName(field.getName());
                    ELITE_TO_KEYPROCESSOR_MAP.put(eliteKeyName, field.getInt(null));
                }
            }
            ELITE_TO_KEYPROCESSOR_MAP.put("Key_Apps", KeyProcessor.KEY_MENU);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize key mappings", e);
        }
    }

    private static String convertToEliteKeyName(String fieldName) {
        String keyName = fieldName.substring(4);
        if (keyName.startsWith("NUM_")) {
            keyName = "Numpad_" + keyName.substring(4);
        }
        StringBuilder eliteKey = new StringBuilder("Key_");
        boolean capitalizeNext = false;
        for (char c : keyName.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                eliteKey.append(capitalizeNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                capitalizeNext = false;
            }
        }
        return eliteKey.toString();
    }

    public KeyBindingExecutor() throws Exception {
        this.keyProcessor = KeyProcessor.getInstance();
    }

    public void executeBinding(KeyBindingsParser.KeyBinding binding) {
        try {
            Integer mainKeyCode = ELITE_TO_KEYPROCESSOR_MAP.get(binding.key);
            if (mainKeyCode == null) {
                log.error("No KeyProcessor mapping for key: {}", binding.key);
                return;
            }
            int[] modifierCodes = new int[binding.modifiers.length];
            for (int i = 0; i < binding.modifiers.length; i++) {
                Integer modCode = ELITE_TO_KEYPROCESSOR_MAP.get(binding.modifiers[i]);
                if (modCode == null) {
                    log.error("No KeyProcessor mapping for modifier: {}", binding.modifiers[i]);
                    return;
                }
                modifierCodes[i] = modCode;
            }
            int[] keyCodes = new int[modifierCodes.length + 1];
            System.arraycopy(modifierCodes, 0, keyCodes, 0, modifierCodes.length);
            keyCodes[modifierCodes.length] = mainKeyCode;
            if (binding.hold) {
                for (int keyCode : keyCodes) {
                    keyProcessor.holdKey(keyCode);
                }
                Thread.sleep(100);
                for (int i = keyCodes.length - 1; i >= 0; i--) {
                    keyProcessor.releaseKey(keyCodes[i]);
                }
                log.debug("Executed hold binding: key={}, modifiers={}", binding.key, binding.modifiers);
            } else {
                keyProcessor.pressKeyCombo(keyCodes);
                log.debug("Executed combo binding: key={}, modifiers={}", binding.key, binding.modifiers);
            }
        } catch (Exception e) {
            log.error("Error executing key binding: {}", e.getMessage());
        }
    }
}
package elite.companion.comms.ai.robot;

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
                    ELITE_TO_KEYPROCESSOR_MAP.put(eliteKeyName.toUpperCase(), field.getInt(null));
                }
            }
            ELITE_TO_KEYPROCESSOR_MAP.put("KEY_APPS", KeyProcessor.KEY_MENU);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize key mappings", e);
        }
    }

    private static String convertToEliteKeyName(String fieldName) {
        String keyName = fieldName.substring(4);
        if (keyName.startsWith("NUM_")) {
            keyName = "Numpad_" + keyName.substring(4);
        }
        StringBuilder eliteKey = new StringBuilder("KEY_");

        for (char c : keyName.toCharArray()) {
            if (c == '_') {
                eliteKey.append(c);
            } else {
                eliteKey.append(Character.toUpperCase(c));
            }
        }
        return eliteKey.toString().toUpperCase();
    }

    public KeyBindingExecutor() throws Exception {
        this.keyProcessor = KeyProcessor.getInstance();
    }

    public void executeBinding(KeyBindingsParser.KeyBinding binding) {
        executeBindingWithHold(binding, 0); // Default: no hold
    }

    public void executeBindingWithHold(KeyBindingsParser.KeyBinding binding, int holdTimeMs) {
        try {
            Integer mainKeyCode = ELITE_TO_KEYPROCESSOR_MAP.get(binding.key.toUpperCase());
            if (mainKeyCode == null) {
                log.error("No KeyProcessor mapping for key: {}", binding.key.toUpperCase());
                return;
            }
            int[] modifierCodes = new int[binding.modifiers.length];
            for (int i = 0; i < binding.modifiers.length; i++) {
                Integer modCode = ELITE_TO_KEYPROCESSOR_MAP.get(binding.modifiers[i].toUpperCase());
                if (modCode == null) {
                    log.error("No KeyProcessor mapping for modifier: {}", binding.modifiers[i]);
                    return;
                }
                modifierCodes[i] = modCode;
            }

            // Press modifiers
            for (int modCode : modifierCodes) {
                keyProcessor.holdKey(modCode);
            }

            // Execute main key
            if (holdTimeMs > 0) {
                keyProcessor.pressAndHoldKey(mainKeyCode, holdTimeMs);
                log.debug("Executed hold binding: key={}, modifiers={}, holdTimeMs={}", binding.key, binding.modifiers, holdTimeMs);
            } else if (binding.hold) {
                keyProcessor.holdKey(mainKeyCode);
                Thread.sleep(500);
                keyProcessor.releaseKey(mainKeyCode);
                log.debug("Executed hold binding: key={}, modifiers={}", binding.key, binding.modifiers);
            } else {
                keyProcessor.pressKey(mainKeyCode);
                log.debug("Executed press binding: key={}, modifiers={}", binding.key, binding.modifiers);
            }

            // Release modifiers in reverse order
            for (int i = modifierCodes.length - 1; i >= 0; i--) {
                keyProcessor.releaseKey(modifierCodes[i]);
            }
        } catch (Exception e) {
            // Ensure all keys are released on error
            for (int modCode : ELITE_TO_KEYPROCESSOR_MAP.values()) {
                keyProcessor.releaseKey(modCode);
            }
            keyProcessor.releaseKey(ELITE_TO_KEYPROCESSOR_MAP.getOrDefault(binding.key, 0));
            log.error("Error executing key binding: {}", e.getMessage());
        }
    }
}
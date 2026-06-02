package elite.intel.ui.view;

import elite.intel.ai.hands.KeyBindingsParser;

import java.util.Arrays;
import java.util.Comparator;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Display-only formatter for Elite Dangerous binding slots.
 * <p>
 * It deliberately keeps raw action/device identifiers intact; this class only
 * decides how diagnostic slot values are presented in the Bindings tab.
 */
class BindingSlotDisplayFormatter {

    String formatSlot(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        if (isEmptySlot(slot)) {
            return getText("bindings.status.notDefined");
        }
        return formatDevice(slot) + " | " + formatBinding(slot);
    }

    String formatBindingToken(String token) {
        if (token.startsWith("Key_") && token.length() > "Key_".length()) {
            return "Key '" + token.substring("Key_".length()) + "'";
        }
        if (token.startsWith("Joy_") && token.length() > "Joy_".length()) {
            return "Joystick " + token.substring("Joy_".length());
        }
        return token;
    }

    private boolean isEmptySlot(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        return isEmptyDevice(slot)
                && (slot == null || slot.key() == null || slot.key().isBlank() || "Key_".equals(slot.key()));
    }

    private String formatDevice(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        if (isEmptyDevice(slot)) {
            return "\u2014";
        }
        return isRawDeviceId(slot.device()) ? "Device " + slot.device() : slot.device();
    }

    private boolean isEmptyDevice(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        return slot == null || slot.device() == null || slot.device().isBlank() || "{NoDevice}".equals(slot.device());
    }

    private boolean isRawDeviceId(String device) {
        return device.matches("(?i)[0-9a-f]{8}");
    }

    private String formatBinding(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        if (slot == null || slot.key() == null || slot.key().isBlank() || "Key_".equals(slot.key())) {
            return getText("bindings.status.notDefined");
        }

        String[] modifiers = slot.modifiers() == null
                ? new String[0]
                : Arrays.stream(slot.modifiers())
                        .filter(modifier -> modifier != null && !modifier.isBlank() && !"Key_".equals(modifier))
                        .map(this::formatBindingToken)
                        .sorted(Comparator.naturalOrder())
                        .toArray(String[]::new);
        String key = formatBindingToken(slot.key());
        if (modifiers.length == 0)
            return key;

        return String.join(" + ", modifiers) + " + " + key;
    }
}

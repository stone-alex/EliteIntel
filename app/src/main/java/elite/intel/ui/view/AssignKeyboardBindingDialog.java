package elite.intel.ui.view;

import elite.intel.ai.hands.BindingSlotType;
import elite.intel.ai.hands.KeyBindingsParser;
import elite.intel.ai.hands.KeyboardKeyAvailabilityService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;
import static elite.intel.ui.view.AppTheme.*;

/**
 * Modal selector for one keyboard binding edit.
 * <p>
 * The dialog only returns the selected slot and key token (or clear request);
 * all file validation and XML writing remain in {@code BindingsWriter}.
 */
public class AssignKeyboardBindingDialog extends JDialog {
    private final KeyboardKeyAvailabilityService availabilityService;
    private final BindingSlotDisplayFormatter slotFormatter = new BindingSlotDisplayFormatter();
    private final Path bindingsFile;
    private final String bindingId;
    private final BindingSlotType slotType;
    private final KeyBindingsParser.ReadOnlyBindingSlot currentSlot;
    private final String originalKey;
    private final boolean alreadyCleared;
    private final JComboBox<KeyOption> keyCombo;
    private final JButton saveButton;
    private final JLabel noFreeKeysLabel;
    private AssignKeyboardBindingSelection selection;

    public AssignKeyboardBindingDialog(
            Component parent,
            Path bindingsFile,
            String bindingId,
            BindingSlotType slotType,
            KeyBindingsParser.ReadOnlyBindingSlot currentSlot,
            KeyboardKeyAvailabilityService availabilityService
    ) {
        super(SwingUtilities.getWindowAncestor(parent), getText("bindings.assign.dialogTitle"), ModalityType.APPLICATION_MODAL);
        this.bindingsFile = bindingsFile;
        this.bindingId = bindingId;
        this.slotType = slotType;
        this.currentSlot = currentSlot;
        this.availabilityService = availabilityService;
        this.originalKey = currentKeyboardKey(currentSlot);
        this.alreadyCleared = isClearedSlot(currentSlot);
        this.keyCombo = new JComboBox<>();
        this.saveButton = makeButton(getText("button.save"));
        this.noFreeKeysLabel = new JLabel(getText("bindings.assign.noFreeKeys"));
        buildUi();
        refreshAvailableKeys();
    }

    public Optional<AssignKeyboardBindingSelection> showDialog() {
        setVisible(true);
        return Optional.ofNullable(selection);
    }

    private void buildUi() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(14, 14, 14, 14));
        GridBagConstraints gbc = baseGbc();
        gbc.insets = new Insets(6, 6, 6, 6);

        addLabel(content, getText("bindings.assign.selectedBinding"), gbc);
        addValue(content, bindingId, gbc);

        nextRow(gbc);
        addLabel(content, getText("bindings.assign.slot"), gbc);
        addValue(content, slotLabel(), gbc);

        nextRow(gbc);
        addLabel(content, getText("bindings.assign.currentValue"), gbc);
        addValue(content, slotFormatter.formatSlot(currentSlot), gbc);

        nextRow(gbc);
        addLabel(content, getText("bindings.assign.newKey"), gbc);
        styleCombo(keyCombo);
        keyCombo.setRenderer(new KeyOptionRenderer());
        keyCombo.addActionListener(e -> updateSaveState());
        addField(content, keyCombo, gbc, 1, 1.0);

        nextRow(gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        noFreeKeysLabel.setForeground(FG_MUTED);
        content.add(noFreeKeysLabel, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(0, 20, 14, 20));
        JButton cancelButton = makeButtonSubtle(getText("button.cancel"));
        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> saveSelection());
        buttons.add(cancelButton);
        buttons.add(saveButton);

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(BG);
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.add(buttons, BorderLayout.SOUTH);
        setContentPane(wrapper);

        getRootPane().setDefaultButton(saveButton);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(620, 300));
        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void addValue(JPanel panel, String value, GridBagConstraints gbc) {
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField field = new JTextField(value);
        field.setEditable(false);
        field.setBackground(BG_PANEL);
        field.setForeground(FG);
        panel.add(field, gbc);
    }

    private void refreshAvailableKeys() {
        keyCombo.removeAllItems();
        try {
            if (originalKey == null && !alreadyCleared) {
                keyCombo.addItem(KeyOption.placeholder(slotFormatter.formatSlot(currentSlot)));
            }
            keyCombo.addItem(KeyOption.clear(getText("bindings.status.notDefined")));

            List<String> keys = availabilityService.availableKeys(bindingsFile, bindingId, slotType);
            if (originalKey != null && !keys.contains(originalKey)) {
                keyCombo.addItem(KeyOption.key(originalKey, formatKeyToken(originalKey)));
            }
            for (String key : keys) {
                keyCombo.addItem(KeyOption.key(key, formatKeyToken(key)));
            }

            selectOriginal();
            boolean canChange = containsDifferentKey();
            keyCombo.setEnabled(canChange || originalKey != null);
            noFreeKeysLabel.setVisible(!canChange);
        } catch (Exception e) {
            keyCombo.setEnabled(false);
            noFreeKeysLabel.setVisible(true);
        }
        updateSaveState();
        revalidate();
        repaint();
    }

    private void saveSelection() {
        KeyOption selectedKey = (KeyOption) keyCombo.getSelectedItem();
        if (selectedKey == null || selectedKey.placeholder() || !isChanged(selectedKey)) {
            return;
        }

        selection = new AssignKeyboardBindingSelection(slotType, selectedKey.rawKey());
        dispose();
    }

    private void updateSaveState() {
        KeyOption selectedKey = (KeyOption) keyCombo.getSelectedItem();
        saveButton.setEnabled(selectedKey != null && !selectedKey.placeholder() && isChanged(selectedKey));
    }

    private boolean containsDifferentKey() {
        for (int i = 0; i < keyCombo.getItemCount(); i++) {
            KeyOption option = keyCombo.getItemAt(i);
            if (!option.placeholder() && isChanged(option)) {
                return true;
            }
        }
        return false;
    }

    private boolean isChanged(KeyOption selectedKey) {
        if (selectedKey.clear()) {
            return !alreadyCleared;
        }
        String key = selectedKey.rawKey();
        return originalKey == null ? key != null : !originalKey.equals(key);
    }

    private void selectOriginal() {
        for (int i = 0; i < keyCombo.getItemCount(); i++) {
            KeyOption option = keyCombo.getItemAt(i);
            if ((originalKey == null && option.rawKey() == null)
                    || (originalKey != null && originalKey.equals(option.rawKey()))) {
                keyCombo.setSelectedIndex(i);
                return;
            }
        }
        if (keyCombo.getItemCount() > 0) {
            keyCombo.setSelectedIndex(0);
        }
    }

    private String currentKeyboardKey(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        if (slot == null || !"Keyboard".equals(slot.device()) || slot.key() == null
                || slot.key().isBlank() || "{NoDevice}".equals(slot.key()) || "Key_".equals(slot.key())) {
            return null;
        }
        return slot.key();
    }

    private boolean isClearedSlot(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        return slot == null || ("{NoDevice}".equals(slot.device())
                && (slot.key() == null || slot.key().isBlank()));
    }

    private String slotLabel() {
        return slotType == BindingSlotType.PRIMARY
                ? getText("bindings.column.primary")
                : getText("bindings.column.secondary");
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(BG_PANEL);
        combo.setForeground(FG);
    }

    private String formatKeyToken(String token) {
        if (token.startsWith("Key_") && token.length() > "Key_".length()) {
            return getText("bindings.assign.keyDisplay", token.substring("Key_".length()), token);
        }
        return token;
    }

    private record KeyOption(String rawKey, String label, boolean placeholder, boolean clear) {
        private static KeyOption placeholder(String label) {
            return new KeyOption(null, label, true, false);
        }

        private static KeyOption clear(String label) {
            return new KeyOption(null, label, false, true);
        }

        private static KeyOption key(String rawKey, String label) {
            return new KeyOption(rawKey, label, false, false);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class KeyOptionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof KeyOption option) {
                label.setText(option.label());
                if (option.placeholder() && !isSelected) {
                    label.setForeground(FG_MUTED);
                }
            }
            return label;
        }
    }
}

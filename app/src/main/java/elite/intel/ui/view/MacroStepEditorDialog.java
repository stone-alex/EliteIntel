package elite.intel.ui.view;

import elite.intel.ai.brain.actions.macro.MacroStep;
import elite.intel.ai.hands.BindingModifier;
import elite.intel.ai.hands.KeyBindingExecutor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Modal editor for one macro step.
 */
final class MacroStepEditorDialog extends JDialog {

    private final JComboBox<MacroStep.Type> typeCombo = buildTypeCombo();
    private final JLabel valueLabel = new JLabel();
    private final JTextField valueField = new JTextField(32);
    private final JLabel commandLabel = new JLabel(getText("actions.macros.editor.step.commandId"));
    private final List<MacroStepPickerItem> commandItems = new ArrayList<>(MacroStepPickerItem.builtInCommandItems());
    private final JComboBox<MacroStepPickerItem> commandCombo = picker(commandItems);
    private final JLabel bindingLabel = new JLabel(getText("actions.macros.editor.step.bindingId"));
    private final List<MacroStepPickerItem> bindingItems = new ArrayList<>(MacroStepPickerItem.bindingItems());
    private final JComboBox<MacroStepPickerItem> bindingCombo = picker(bindingItems);
    private final JLabel rawKeyLabel = new JLabel(getText("actions.macros.editor.step.rawKey"));
    private final List<MacroStepPickerItem> rawKeyItems = buildRawKeyPickerItems();
    private final JComboBox<MacroStepPickerItem> rawKeyCombo = picker(rawKeyItems);
    private final JLabel rawModLabel = new JLabel(getText("actions.macros.editor.step.rawKeyModifier"));
    private final JComboBox<RawModOption> rawModCombo = buildRawModCombo();
    private final JLabel durationLabel = new JLabel(getText("actions.macros.editor.step.durationMs"));
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(250, 0, Integer.MAX_VALUE, 50));
    private MacroStep result;

    MacroStepEditorDialog(Component parent, MacroStep step) {
        super(SwingUtilities.getWindowAncestor(parent), getText("actions.macros.editor.step.title"), ModalityType.APPLICATION_MODAL);
        populate(step);
        buildUi();
        updateFieldsForType();
    }

    MacroStep showDialog() {
        setVisible(true);
        return result;
    }

    private void populate(MacroStep step) {
        if (step == null) {
            typeCombo.setSelectedItem(MacroStep.Type.SPEAK);
            return;
        }
        typeCombo.setSelectedItem(step.getType());
        durationSpinner.setValue(Math.max(0, step.getDurationMs()));
        switch (step.getType()) {
            case SPEAK -> valueField.setText(step.getText());
            case BINDING_TAP, BINDING_HOLD ->
                    selectPickerItem(bindingCombo, bindingItems, step.getBindingId(), getText("actions.macros.editor.step.unknownBinding"));
            case RUN_COMMAND ->
                    selectPickerItem(commandCombo, commandItems, step.getActionId(), getText("actions.macros.editor.step.unknownCommand"));
            case DELAY -> valueField.setText("");
            case RAW_KEY -> {
                selectPickerItem(rawKeyCombo, rawKeyItems, step.getRawKey(), getText("actions.macros.editor.step.unknownRawKey"));
                selectRawMod(step.getRawKeyModifier());
            }
        }
    }

    private void buildUi() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(AppTheme.BG);
        content.setBorder(new EmptyBorder(16, 18, 12, 18));
        content.add(form(), BorderLayout.CENTER);
        content.add(buttons(), BorderLayout.SOUTH);
        setContentPane(content);

        typeCombo.addActionListener(event -> updateFieldsForType());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(560, 260));
        setLocationRelativeTo(getOwner());
    }

    private JPanel form() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG);
        GridBagConstraints gbc = AppTheme.baseGbc();

        addRow(panel, gbc, getText("actions.macros.editor.step.type"), typeCombo);
        addRow(panel, gbc, valueLabel, valueField);
        addRow(panel, gbc, commandLabel, commandCombo);
        addRow(panel, gbc, bindingLabel, bindingCombo);
        addRow(panel, gbc, rawKeyLabel, rawKeyCombo);
        addRow(panel, gbc, rawModLabel, rawModCombo);
        addRow(panel, gbc, durationLabel, durationSpinner);
        AppTheme.applyDarkPalette(panel);
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, JComponent field) {
        addRow(panel, gbc, new JLabel(label), field);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, JLabel label, JComponent field) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        label.setForeground(AppTheme.FG_MUTED);
        label.setPreferredSize(new Dimension(160, 28));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private JPanel buttons() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JButton save = AppTheme.makeButton(getText("button.save"));
        save.addActionListener(event -> save());
        panel.add(save, BorderLayout.WEST);

        JButton cancel = AppTheme.makeButtonSubtle(getText("button.cancel"));
        cancel.addActionListener(event -> dispose());
        panel.add(cancel, BorderLayout.EAST);
        getRootPane().setDefaultButton(save);
        return panel;
    }

    private void updateFieldsForType() {
        MacroStep.Type type = selectedType();
        boolean hasText = type == MacroStep.Type.SPEAK;
        boolean hasCommand = type == MacroStep.Type.RUN_COMMAND;
        boolean hasBinding = type == MacroStep.Type.BINDING_TAP || type == MacroStep.Type.BINDING_HOLD;
        boolean isRawKey = type == MacroStep.Type.RAW_KEY;
        boolean hasDuration = type == MacroStep.Type.BINDING_HOLD || type == MacroStep.Type.DELAY || isRawKey;

        valueLabel.setVisible(hasText);
        valueField.setVisible(hasText);
        commandLabel.setVisible(hasCommand);
        commandCombo.setVisible(hasCommand);
        bindingLabel.setVisible(hasBinding);
        bindingCombo.setVisible(hasBinding);
        rawKeyLabel.setVisible(isRawKey);
        rawKeyCombo.setVisible(isRawKey);
        rawModLabel.setVisible(isRawKey);
        rawModCombo.setVisible(isRawKey);
        durationLabel.setVisible(hasDuration);
        durationSpinner.setVisible(hasDuration);

        valueLabel.setText(getText("actions.macros.editor.step.text"));
        pack();
    }

    private void save() {
        MacroStep step = buildStep();
        try {
            step.validate(0);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return;
        }
        result = step;
        dispose();
    }

    private MacroStep buildStep() {
        MacroStep.Type type = selectedType();
        String value = valueField.getText().trim();
        int duration = ((Number) durationSpinner.getValue()).intValue();
        return switch (type) {
            case SPEAK -> new MacroStep(type, null, 0, value, null);
            case BINDING_TAP -> new MacroStep(type, selectedPickerId(bindingCombo), 0, null, null);
            case BINDING_HOLD -> new MacroStep(type, selectedPickerId(bindingCombo), duration, null, null);
            case DELAY -> new MacroStep(type, null, duration, null, null);
            case RUN_COMMAND -> new MacroStep(type, null, 0, null, selectedPickerId(commandCombo));
            case RAW_KEY -> {
                String rawKey = selectedPickerId(rawKeyCombo);
                RawModOption modOption = (RawModOption) rawModCombo.getSelectedItem();
                String rawMod = (modOption != null && !modOption.key().isBlank()) ? modOption.key() : null;
                yield new MacroStep(type, null, duration, null, null, rawKey, rawMod);
            }
        };
    }

    private MacroStep.Type selectedType() {
        Object selected = typeCombo.getSelectedItem();
        return selected instanceof MacroStep.Type type ? type : MacroStep.Type.SPEAK;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, getText("actions.macros.editor.validation.title"), JOptionPane.ERROR_MESSAGE);
    }

    private static JComboBox<MacroStepPickerItem> picker(List<MacroStepPickerItem> items) {
        JComboBox<MacroStepPickerItem> combo = new JComboBox<>(new DefaultComboBoxModel<>(items.toArray(MacroStepPickerItem[]::new)));
        combo.setEditable(true);
        configureSearch(combo, items);
        return combo;
    }

    private static void configureSearch(JComboBox<MacroStepPickerItem> combo, List<MacroStepPickerItem> sourceItems) {
        Component editorComponent = combo.getEditor().getEditorComponent();
        if (!(editorComponent instanceof JTextComponent editor)) {
            return;
        }

        final boolean[] updating = {false};
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                if (updating[0] || !editor.hasFocus()) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    String query = editor.getText();
                    DefaultComboBoxModel<MacroStepPickerItem> model = new DefaultComboBoxModel<>();
                    sourceItems.stream()
                            .filter(item -> item.matches(query))
                            .forEach(model::addElement);
                    updating[0] = true;
                    combo.setModel(model);
                    combo.setSelectedItem(query);
                    updating[0] = false;
                    if (combo.isShowing() && model.getSize() > 0) {
                        combo.showPopup();
                    }
                });
            }
        });
    }

    private static void selectPickerItem(
            JComboBox<MacroStepPickerItem> combo,
            List<MacroStepPickerItem> items,
            String id,
            String unknownLabel
    ) {
        if (id == null || id.isBlank()) {
            return;
        }
        for (MacroStepPickerItem item : items) {
            if (item.id().equalsIgnoreCase(id)) {
                combo.setSelectedItem(item);
                return;
            }
        }
        MacroStepPickerItem unknown = MacroStepPickerItem.unknown(id, unknownLabel);
        items.add(0, unknown);
        combo.addItem(unknown);
        combo.setSelectedItem(unknown);
    }

    private static String selectedPickerId(JComboBox<MacroStepPickerItem> combo) {
        return MacroStepPickerItem.resolveId(combo.getEditor().getItem()).trim();
    }

    private static JComboBox<MacroStep.Type> buildTypeCombo() {
        JComboBox<MacroStep.Type> combo = new JComboBox<>(MacroStep.Type.values());
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof MacroStep.Type t ? stepTypeLabel(t) : String.valueOf(value));
                return this;
            }
        });
        return combo;
    }

    /** Returns the localized display label for a step type. */
    static String stepTypeLabel(MacroStep.Type type) {
        if (type == null) return "";
        return switch (type) {
            case BINDING_TAP  -> getText("actions.macros.editor.step.type.bindingTap");
            case BINDING_HOLD -> getText("actions.macros.editor.step.type.bindingHold");
            case DELAY        -> getText("actions.macros.editor.step.type.delay");
            case SPEAK        -> getText("actions.macros.editor.step.type.speak");
            case RUN_COMMAND  -> getText("actions.macros.editor.step.type.runCommand");
            case RAW_KEY      -> getText("actions.macros.editor.step.type.rawKey");
        };
    }

    private static List<MacroStepPickerItem> buildRawKeyPickerItems() {
        BindingSlotDisplayFormatter formatter = new BindingSlotDisplayFormatter();
        return KeyBindingExecutor.knownEliteKeyNames().stream()
                .sorted()
                .map(name -> new MacroStepPickerItem(name, formatter.formatBindingToken(BindingSlotDisplayFormatter.toEliteKeyFormat(name)), true))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private static JComboBox<RawModOption> buildRawModCombo() {
        List<RawModOption> items = new ArrayList<>();
        items.add(new RawModOption("", getText("actions.macros.editor.step.noModifier")));
        BindingModifier.supportedKeyboardModifiers().forEach(bm ->
                items.add(new RawModOption(bm.key().toUpperCase(), bm.key())));
        JComboBox<RawModOption> combo = new JComboBox<>(items.toArray(RawModOption[]::new));
        combo.setBackground(AppTheme.BG_PANEL);
        combo.setForeground(AppTheme.FG);
        return combo;
    }

    /** Selects the modifier combo item matching the stored key name (case-insensitive), or "(none)" if absent. */
    private void selectRawMod(String storedKeyName) {
        for (int i = 0; i < rawModCombo.getItemCount(); i++) {
            RawModOption option = rawModCombo.getItemAt(i);
            if (option.key().equalsIgnoreCase(storedKeyName != null ? storedKeyName : "")) {
                rawModCombo.setSelectedIndex(i);
                return;
            }
        }
        rawModCombo.setSelectedIndex(0); // default to "(none)"
    }

    /** Carries the stored uppercase key name and a human-readable display label for the modifier combo. */
    private record RawModOption(String key, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}

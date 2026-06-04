package elite.intel.ui.view;

import elite.intel.ai.brain.actions.macro.MacroStep;

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

    private final JComboBox<MacroStep.Type> typeCombo = new JComboBox<>(MacroStep.Type.values());
    private final JLabel valueLabel = new JLabel();
    private final JTextField valueField = new JTextField(32);
    private final JLabel commandLabel = new JLabel(getText("actions.macros.editor.step.commandId"));
    private final List<MacroStepPickerItem> commandItems = new ArrayList<>(MacroStepPickerItem.builtInCommandItems());
    private final JComboBox<MacroStepPickerItem> commandCombo = picker(commandItems);
    private final JLabel bindingLabel = new JLabel(getText("actions.macros.editor.step.bindingId"));
    private final List<MacroStepPickerItem> bindingItems = new ArrayList<>(MacroStepPickerItem.bindingItems());
    private final JComboBox<MacroStepPickerItem> bindingCombo = picker(bindingItems);
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
        boolean hasDuration = type == MacroStep.Type.BINDING_HOLD || type == MacroStep.Type.DELAY;

        valueLabel.setVisible(hasText);
        valueField.setVisible(hasText);
        commandLabel.setVisible(hasCommand);
        commandCombo.setVisible(hasCommand);
        bindingLabel.setVisible(hasBinding);
        bindingCombo.setVisible(hasBinding);
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
}

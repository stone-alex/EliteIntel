package elite.intel.ui.view;

import elite.intel.ai.brain.actions.macro.MacroParameterSpec;
import elite.intel.ai.brain.actions.macro.MacroStep;
import elite.intel.ai.brain.commons.ActionParameterKeyExtractor;
import elite.intel.ai.hands.BindingModifier;
import elite.intel.ai.hands.KeyBindingExecutor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Modal editor for one macro step.
 */
final class MacroStepEditorDialog extends JDialog {

    private static final int DIALOG_MIN_WIDTH = 760;
    private static final int PICKER_FIELD_WIDTH = 500;
    private static final int PICKER_FIELD_HEIGHT = 42;

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
    private final JLabel stepParamsLabel = new JLabel(getText("actions.macros.editor.step.params"));
    private final JButton stepParamsInfoButton = new JButton("\u24D8");
    private final DefaultTableModel stepParamsModel = new DefaultTableModel(
            new String[]{getText("actions.macros.editor.step.params.column.key"),
                         getText("actions.macros.editor.step.params.column.value")}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return true; }
    };
    private final JTable stepParamsTable = new JTable(stepParamsModel);
    private final JPanel stepParamsPanel = buildStepParamsPanel();
    private final Set<String> macroParameterNames;
    private final Consumer<List<MacroParameterSpec>> missingMacroParamsConsumer;
    private String lastParamHintActionId;
    private MacroStep result;

    MacroStepEditorDialog(Component parent, MacroStep step) {
        this(parent, step, List.of(), missingParams -> {});
    }

    MacroStepEditorDialog(Component parent, MacroStep step, List<MacroParameterSpec> macroParameters) {
        this(parent, step, macroParameters, missingParams -> {});
    }

    MacroStepEditorDialog(
            Component parent,
            MacroStep step,
            List<MacroParameterSpec> macroParameters,
            Consumer<List<MacroParameterSpec>> missingMacroParamsConsumer
    ) {
        super(SwingUtilities.getWindowAncestor(parent), getText("actions.macros.editor.step.title"), ModalityType.APPLICATION_MODAL);
        this.macroParameterNames = macroParameterNames(macroParameters);
        this.missingMacroParamsConsumer = missingMacroParamsConsumer == null ? missingParams -> {} : missingMacroParamsConsumer;
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
            commandCombo.setSelectedItem("");
            return;
        }
        typeCombo.setSelectedItem(step.getType());
        durationSpinner.setValue(Math.max(0, step.getDurationMs()));
        switch (step.getType()) {
            case SPEAK -> valueField.setText(step.getText());
            case BINDING_TAP, BINDING_HOLD ->
                    selectPickerItem(bindingCombo, bindingItems, step.getBindingId(), getText("actions.macros.editor.step.unknownBinding"));
            case RUN_COMMAND -> {
                selectPickerItem(commandCombo, commandItems, step.getActionId(), getText("actions.macros.editor.step.unknownCommand"));
                stepParamsModel.setRowCount(0);
                step.getStepParams().forEach((k, v) -> stepParamsModel.addRow(new Object[]{k, v}));
            }
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
        commandCombo.addActionListener(event -> updateFieldsForSelectedCommand());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(DIALOG_MIN_WIDTH, 260));
        setSize(Math.max(getWidth(), DIALOG_MIN_WIDTH), getHeight());
        setLocationRelativeTo(getOwner());
    }

    private JPanel form() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG);
        GridBagConstraints gbc = AppTheme.baseGbc();

        addRow(panel, gbc, getText("actions.macros.editor.step.type"), typeCombo);
        addRow(panel, gbc, valueLabel, valueField);
        addRow(panel, gbc, commandLabel, commandCombo);

        // Step params panel spans both columns for RUN_COMMAND.
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(stepParamsPanel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;

        addRow(panel, gbc, bindingLabel, bindingCombo);
        addRow(panel, gbc, rawKeyLabel, rawKeyCombo);
        addRow(panel, gbc, rawModLabel, rawModCombo);
        addRow(panel, gbc, durationLabel, durationSpinner);
        AppTheme.applyDarkPalette(panel);
        stylePicker(commandCombo);
        stylePicker(bindingCombo);
        stylePicker(rawKeyCombo);
        styleInfoButton(stepParamsInfoButton);
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
        boolean fixedWidthField = field instanceof JComboBox<?> || field instanceof JSpinner;
        gbc.weightx = fixedWidthField ? 0 : 1;
        gbc.fill = fixedWidthField ? GridBagConstraints.NONE : GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
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
        if (hasCommand) {
            addMissingHintedStepParams();
        }

        valueLabel.setVisible(hasText);
        valueField.setVisible(hasText);
        commandLabel.setVisible(hasCommand);
        commandCombo.setVisible(hasCommand);
        stepParamsPanel.setVisible(shouldShowStepParamsPanel(hasCommand));
        bindingLabel.setVisible(hasBinding);
        bindingCombo.setVisible(hasBinding);
        rawKeyLabel.setVisible(isRawKey);
        rawKeyCombo.setVisible(isRawKey);
        rawModLabel.setVisible(isRawKey);
        rawModCombo.setVisible(isRawKey);
        durationLabel.setVisible(hasDuration);
        durationSpinner.setVisible(hasDuration);

        valueLabel.setText(getText("actions.macros.editor.step.text"));
        packPreservingWidth();
    }

    private void updateFieldsForSelectedCommand() {
        if (selectedType() != MacroStep.Type.RUN_COMMAND) {
            return;
        }
        if (!(commandCombo.getSelectedItem() instanceof MacroStepPickerItem)) {
            return;
        }
        syncStepParamsForSelectedCommand();
        stepParamsPanel.setVisible(shouldShowStepParamsPanel(true));
        packPreservingWidth();
    }

    private void addMissingHintedStepParams() {
        String actionId = selectedPickerId(commandCombo);
        if (actionId.isBlank() || actionId.equalsIgnoreCase(lastParamHintActionId != null ? lastParamHintActionId : "")) {
            return;
        }
        lastParamHintActionId = actionId;

        for (String key : ActionParameterKeyExtractor.getInstance().parameterKeysForAction(actionId)) {
            if (!hasStepParamKey(key)) {
                stepParamsModel.addRow(new Object[]{key, defaultStepParamValue(key)});
            }
        }
    }

    private void syncStepParamsForSelectedCommand() {
        String actionId = selectedPickerId(commandCombo);
        if (actionId.isBlank() || actionId.equalsIgnoreCase(lastParamHintActionId != null ? lastParamHintActionId : "")) {
            return;
        }
        lastParamHintActionId = actionId;

        List<String> knownKeys = ActionParameterKeyExtractor.getInstance().parameterKeysForAction(actionId);
        stepParamsModel.setRowCount(0);
        if (knownKeys.isEmpty()) {
            return;
        }

        for (String key : knownKeys) {
            stepParamsModel.addRow(new Object[]{key, defaultStepParamValue(key)});
        }
    }

    private boolean shouldShowStepParamsPanel(boolean hasCommand) {
        if (!hasCommand) {
            return false;
        }
        if (stepParamsModel.getRowCount() > 0) {
            return true;
        }
        return !ActionParameterKeyExtractor.getInstance()
                .parameterKeysForAction(selectedPickerId(commandCombo))
                .isEmpty();
    }

    private boolean hasStepParamKey(String key) {
        for (int i = 0; i < stepParamsModel.getRowCount(); i++) {
            Object existing = stepParamsModel.getValueAt(i, 0);
            if (key.equals(existing != null ? existing.toString().trim() : "")) {
                return true;
            }
        }
        return false;
    }

    private String defaultStepParamValue(String key) {
        return "${" + key + "}";
    }

    private void save() {
        MacroStep step = buildStep();
        try {
            step.validate(0);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return;
        }
        addMissingMacroParams(step);
        result = step;
        dispose();
    }

    private void addMissingMacroParams(MacroStep step) {
        if (step.getType() != MacroStep.Type.RUN_COMMAND) {
            return;
        }
        List<MacroParameterSpec> missing = step.getStepParams().entrySet().stream()
                .filter(entry -> isSameNameMacroTemplate(entry.getKey(), entry.getValue()))
                .map(Map.Entry::getKey)
                .filter(key -> !macroParameterNames.contains(key))
                .distinct()
                .map(key -> new MacroParameterSpec(
                        key,
                        inferredMacroParamType(step.getActionId(), key),
                        true,
                        stepTypeLabel(step.getType()) + " step parameter '" + key + "'.",
                        List.of(),
                        null
                ))
                .toList();
        if (!missing.isEmpty()) {
            missingMacroParamsConsumer.accept(missing);
        }
    }

    private static boolean isSameNameMacroTemplate(String key, String value) {
        return key != null && !key.isBlank() && ("${" + key + "}").equals(value);
    }

    private static String inferredMacroParamType(String actionId, String key) {
        return ActionParameterKeyExtractor.getInstance().parameterHintsForAction(actionId).stream()
                .filter(hint -> hint.name().equals(key))
                .map(ActionParameterKeyExtractor.ActionParameterHint::type)
                .findFirst()
                .orElse("string");
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
            case RUN_COMMAND -> {
                // Stop any in-progress cell edit so the last typed value is committed.
                if (stepParamsTable.isEditing()) {
                    stepParamsTable.getCellEditor().stopCellEditing();
                }
                Map<String, String> paramMap = new LinkedHashMap<>();
                for (int i = 0; i < stepParamsModel.getRowCount(); i++) {
                    Object k = stepParamsModel.getValueAt(i, 0);
                    Object v = stepParamsModel.getValueAt(i, 1);
                    String key = k != null ? k.toString().trim() : "";
                    String val = v != null ? v.toString().trim() : "";
                    if (!key.isBlank()) {
                        paramMap.put(key, val);
                    }
                }
                yield paramMap.isEmpty()
                        ? new MacroStep(type, null, 0, null, selectedPickerId(commandCombo))
                        : MacroStep.runCommandWithParams(selectedPickerId(commandCombo), paramMap);
            }
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
        combo.setPreferredSize(new Dimension(PICKER_FIELD_WIDTH, PICKER_FIELD_HEIGHT));
        combo.setMinimumSize(new Dimension(PICKER_FIELD_WIDTH, PICKER_FIELD_HEIGHT));
        stylePicker(combo);
        configureSearch(combo, items);
        return combo;
    }

    private static void stylePicker(JComboBox<MacroStepPickerItem> combo) {
        combo.setBackground(AppTheme.BG_PANEL);
        combo.setForeground(AppTheme.FG);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "" : value.toString());
                setBackground(isSelected ? AppTheme.SEL_BG : AppTheme.BG_PANEL);
                setForeground(isSelected ? AppTheme.SEL_FG : AppTheme.FG);
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return this;
            }
        });

        Component editorComponent = combo.getEditor().getEditorComponent();
        if (editorComponent instanceof JTextComponent editor) {
            editor.setBackground(AppTheme.BG_PANEL);
            editor.setForeground(AppTheme.FG);
            editor.setCaretColor(AppTheme.FG);
            editor.setSelectionColor(AppTheme.SEL_BG);
            editor.setSelectedTextColor(AppTheme.SEL_FG);
            editor.setBorder(new EmptyBorder(4, 10, 4, 8));
        }
    }

    private static void configureSearch(JComboBox<MacroStepPickerItem> combo, List<MacroStepPickerItem> sourceItems) {
        Component editorComponent = combo.getEditor().getEditorComponent();
        if (!(editorComponent instanceof JTextComponent editor)) {
            return;
        }

        final boolean[] updating = {false};
        combo.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (combo.getSelectedItem() instanceof MacroStepPickerItem selected
                        && combo.getModel().getSize() != sourceItems.size()) {
                    updating[0] = true;
                    combo.setModel(new DefaultComboBoxModel<>(sourceItems.toArray(MacroStepPickerItem[]::new)));
                    combo.setSelectedItem(selected);
                    updating[0] = false;
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
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
                    MacroStepPickerItem exactDisplayItem = findByDisplayText(sourceItems, query);
                    if (exactDisplayItem != null) {
                        updating[0] = true;
                        combo.setModel(new DefaultComboBoxModel<>(sourceItems.toArray(MacroStepPickerItem[]::new)));
                        combo.setSelectedItem(exactDisplayItem);
                        updating[0] = false;
                        return;
                    }

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

    private static MacroStepPickerItem findByDisplayText(List<MacroStepPickerItem> items, String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (MacroStepPickerItem item : items) {
            if (item.toString().equals(text)) {
                return item;
            }
        }
        return null;
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

    private JPanel buildStepParamsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        header.setOpaque(false);
        stepParamsLabel.setForeground(AppTheme.FG_MUTED);
        header.add(stepParamsLabel);
        stepParamsInfoButton.addActionListener(event -> showStepParamsInfo());
        header.add(stepParamsInfoButton);
        panel.add(header, BorderLayout.NORTH);

        stepParamsTable.setBackground(AppTheme.BG_PANEL);
        stepParamsTable.setForeground(AppTheme.FG);
        stepParamsTable.setSelectionBackground(AppTheme.SEL_BG);
        stepParamsTable.setSelectionForeground(AppTheme.SEL_FG);
        stepParamsTable.getTableHeader().setBackground(AppTheme.BG);
        stepParamsTable.getTableHeader().setForeground(AppTheme.FG);
        stepParamsTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(stepParamsTable);
        scroll.setPreferredSize(new Dimension(0, 90));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        JButton add = AppTheme.makeButtonSubtle(getText("actions.macros.editor.step.params.add"));
        add.addActionListener(e -> stepParamsModel.addRow(new Object[]{"", ""}));
        JButton remove = AppTheme.makeButtonSubtle(getText("actions.macros.editor.step.params.remove"));
        remove.addActionListener(e -> {
            int row = stepParamsTable.getSelectedRow();
            if (row >= 0) {
                if (stepParamsTable.isEditing()) stepParamsTable.getCellEditor().cancelCellEditing();
                stepParamsModel.removeRow(row);
                stepParamsPanel.setVisible(shouldShowStepParamsPanel(selectedType() == MacroStep.Type.RUN_COMMAND));
                packPreservingWidth();
            }
        });
        buttons.add(add);
        buttons.add(remove);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private static void styleInfoButton(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setForeground(AppTheme.FG_MUTED);
        button.setBackground(AppTheme.BG);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, button.getFont().getSize2D() + 2f));
        Dimension size = new Dimension(28, 28);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
    }

    private void showStepParamsInfo() {
        JOptionPane.showMessageDialog(
                this,
                getText("actions.macros.editor.step.params.hint"),
                getText("actions.macros.editor.step.params"),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void packPreservingWidth() {
        int width = getWidth() > 0 ? getWidth() : DIALOG_MIN_WIDTH;
        pack();
        setSize(Math.max(width, DIALOG_MIN_WIDTH), getHeight());
        revalidate();
        repaint();
    }

    /** Carries the stored uppercase key name and a human-readable display label for the modifier combo. */
    private record RawModOption(String key, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private static Set<String> macroParameterNames(List<MacroParameterSpec> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Set.of();
        }
        Set<String> names = new LinkedHashSet<>();
        parameters.stream()
                .map(MacroParameterSpec::getName)
                .filter(name -> name != null && !name.isBlank())
                .forEach(names::add);
        return Set.copyOf(names);
    }
}

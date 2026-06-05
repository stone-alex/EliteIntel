package elite.intel.ui.view;

import elite.intel.ai.brain.actions.macro.MacroDefinition;
import elite.intel.ai.brain.actions.macro.MacroEditorValidator;
import elite.intel.ai.brain.actions.macro.MacroStep;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Modal CRUD editor for one macro definition. It returns a validated macro but does not persist it.
 */
final class MacroEditorDialog extends JDialog {

    private final List<MacroDefinition> existingMacros;
    private final String originalId;
    private final JTextField idField = new JTextField(36);
    private final JTextField nameField = new JTextField(36);
    private final JTextArea descriptionArea = textArea(3);
    private final JTextArea phrasesArea = textArea(4);
    private final StepsTableModel stepsModel = new StepsTableModel();
    private final JTable stepsTable = new JTable(stepsModel);
    private final JTextArea errorsArea = textArea(4);
    private MacroDefinition result;

    MacroEditorDialog(Component parent, MacroDefinition macro, List<MacroDefinition> existingMacros) {
        super(
                SwingUtilities.getWindowAncestor(parent),
                macro == null ? getText("actions.macros.editor.newTitle") : getText("actions.macros.editor.editTitle"),
                ModalityType.APPLICATION_MODAL
        );
        this.existingMacros = existingMacros == null ? List.of() : List.copyOf(existingMacros);
        this.originalId = macro == null ? null : macro.getId();
        populate(macro);
        buildUi(macro != null);
    }

    MacroDefinition showDialog() {
        setVisible(true);
        return result;
    }

    private void populate(MacroDefinition macro) {
        if (macro == null) {
            idField.setText("macro_new");
            return;
        }
        idField.setText(macro.getId());
        nameField.setText(macro.getName());
        descriptionArea.setText(macro.getDescription());
        phrasesArea.setText(macro.getPhrases());
        stepsModel.setSteps(macro.getSteps());
    }

    private void buildUi(boolean existing) {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(AppTheme.BG);
        content.setBorder(new EmptyBorder(16, 18, 12, 18));
        content.add(form(existing), BorderLayout.NORTH);
        content.add(stepsPanel(), BorderLayout.CENTER);
        content.add(bottomPanel(), BorderLayout.SOUTH);
        setContentPane(content);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(860, 720));
        setLocationRelativeTo(getOwner());
    }

    private JPanel form(boolean existing) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG);
        GridBagConstraints gbc = AppTheme.baseGbc();
        idField.setEditable(!existing);

        addField(panel, gbc, getText("actions.macros.editor.id"), idField);
        addField(panel, gbc, getText("actions.macros.editor.name"), nameField);
        addArea(panel, gbc, getText("actions.macros.editor.description"), descriptionArea);
        addArea(panel, gbc, getText("actions.macros.editor.phrases"), phrasesArea);
        return panel;
    }

    private JPanel stepsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel label = new JLabel(getText("actions.macros.editor.steps"));
        label.setForeground(AppTheme.FG_MUTED);
        panel.add(label, BorderLayout.NORTH);

        stepsTable.setFillsViewportHeight(true);
        stepsTable.setRowHeight(30);
        stepsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stepsTable.setBackground(AppTheme.BG_PANEL);
        stepsTable.setForeground(AppTheme.FG);
        stepsTable.setSelectionBackground(AppTheme.SEL_BG);
        stepsTable.setSelectionForeground(AppTheme.SEL_FG);
        stepsTable.getTableHeader().setBackground(AppTheme.BG);
        stepsTable.getTableHeader().setForeground(AppTheme.FG);
        panel.add(new JScrollPane(stepsTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        addStepButton(buttons, "actions.macros.editor.step.add", this::addStep);
        addStepButton(buttons, "actions.macros.editor.step.edit", this::editStep);
        addStepButton(buttons, "actions.macros.editor.step.remove", this::removeStep);
        addStepButton(buttons, "actions.macros.editor.step.up", () -> moveSelected(-1));
        addStepButton(buttons, "actions.macros.editor.step.down", () -> moveSelected(1));
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel bottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        errorsArea.setEditable(false);
        errorsArea.setVisible(false);
        panel.add(errorsArea, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setOpaque(false);
        JButton save = AppTheme.makeButton(getText("button.save"));
        save.addActionListener(event -> save());
        buttons.add(save, BorderLayout.WEST);

        JButton cancel = AppTheme.makeButtonSubtle(getText("button.cancel"));
        cancel.addActionListener(event -> dispose());
        buttons.add(cancel, BorderLayout.EAST);
        panel.add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField field) {
        addLabel(panel, gbc, labelText);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        field.setBackground(AppTheme.BG_PANEL);
        field.setForeground(AppTheme.FG);
        field.setCaretColor(AppTheme.FG);
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private void addArea(JPanel panel, GridBagConstraints gbc, String labelText, JTextArea area) {
        addLabel(panel, gbc, labelText);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.getViewport().setBackground(AppTheme.BG_PANEL);
        panel.add(scrollPane, gbc);
        gbc.gridy++;
    }

    private void addLabel(JPanel panel, GridBagConstraints gbc, String labelText) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(labelText);
        label.setForeground(AppTheme.FG_MUTED);
        label.setPreferredSize(new Dimension(170, 28));
        panel.add(label, gbc);
    }

    private void addStepButton(JPanel panel, String key, Runnable action) {
        JButton button = AppTheme.makeButtonSubtle(getText(key));
        button.addActionListener(event -> action.run());
        panel.add(button);
    }

    private void addStep() {
        MacroStep step = new MacroStepEditorDialog(this, null).showDialog();
        if (step != null) {
            stepsModel.addStep(step);
        }
    }

    private void editStep() {
        int row = selectedStepRow();
        if (row < 0) {
            return;
        }
        MacroStep edited = new MacroStepEditorDialog(this, stepsModel.getStep(row)).showDialog();
        if (edited != null) {
            stepsModel.setStep(row, edited);
        }
    }

    private void removeStep() {
        int row = selectedStepRow();
        if (row >= 0) {
            stepsModel.removeStep(row);
        }
    }

    private void moveSelected(int delta) {
        int row = selectedStepRow();
        if (stepsModel.move(row, delta)) {
            stepsTable.setRowSelectionInterval(row + delta, row + delta);
        }
    }

    private int selectedStepRow() {
        int viewRow = stepsTable.getSelectedRow();
        return viewRow < 0 ? -1 : stepsTable.convertRowIndexToModel(viewRow);
    }

    private void save() {
        MacroDefinition candidate = buildCandidate();
        List<String> errors = MacroEditorValidator.validate(candidate, existingMacros, originalId);
        if (!errors.isEmpty()) {
            showErrors(errors);
            return;
        }
        result = candidate;
        dispose();
    }

    private MacroDefinition buildCandidate() {
        String name = nameField.getText().trim();
        String id = idField.getText().trim();
        if (id.isBlank()) {
            id = uniqueGeneratedId(name);
            idField.setText(id);
        }
        return new MacroDefinition(
                id,
                name,
                descriptionArea.getText().trim(),
                phrasesArea.getText().trim(),
                stepsModel.steps()
        );
    }

    private String uniqueGeneratedId(String name) {
        String base = "macro_" + sanitizeId(name);
        if ("macro_".equals(base)) {
            base = "macro_new";
        }
        List<String> existingIds = existingMacros.stream()
                .filter(macro -> !sameId(macro.getId(), originalId))
                .map(MacroDefinition::getId)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .toList();
        String candidate = base;
        int suffix = 2;
        while (existingIds.contains(candidate.toLowerCase(Locale.ROOT))) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }

    private static String sanitizeId(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return normalized;
    }

    private static boolean sameId(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.equalsIgnoreCase(right);
    }

    private void showErrors(List<String> errors) {
        errorsArea.setText(String.join(System.lineSeparator(), errors));
        errorsArea.setVisible(true);
        pack();
    }

    private static JTextArea textArea(int rows) {
        JTextArea area = new JTextArea(rows, 36);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(AppTheme.BG_PANEL);
        area.setForeground(AppTheme.FG);
        area.setCaretColor(AppTheme.FG);
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        return area;
    }

    private static final class StepsTableModel extends AbstractTableModel {
        private final List<MacroStep> steps = new ArrayList<>();
        private final String[] columns = {
                getText("actions.macros.editor.step.column.type"),
                getText("actions.macros.editor.step.column.value"),
                getText("actions.macros.editor.step.column.durationMs")
        };

        private void setSteps(List<MacroStep> newSteps) {
            steps.clear();
            if (newSteps != null) {
                steps.addAll(newSteps);
            }
            fireTableDataChanged();
        }

        private List<MacroStep> steps() {
            return List.copyOf(steps);
        }

        private MacroStep getStep(int row) {
            return steps.get(row);
        }

        private void addStep(MacroStep step) {
            steps.add(step);
            fireTableRowsInserted(steps.size() - 1, steps.size() - 1);
        }

        private void setStep(int row, MacroStep step) {
            steps.set(row, step);
            fireTableRowsUpdated(row, row);
        }

        private void removeStep(int row) {
            steps.remove(row);
            fireTableRowsDeleted(row, row);
        }

        private boolean move(int row, int delta) {
            int target = row + delta;
            if (row < 0 || target < 0 || target >= steps.size()) {
                return false;
            }
            MacroStep step = steps.remove(row);
            steps.add(target, step);
            fireTableDataChanged();
            return true;
        }

        @Override
        public int getRowCount() {
            return steps.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MacroStep step = steps.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> step.getType();
                case 1 -> stepValue(step);
                case 2 -> durationValue(step);
                default -> "";
            };
        }

        private static String stepValue(MacroStep step) {
            return switch (step.getType()) {
                case SPEAK -> step.getText();
                case BINDING_TAP, BINDING_HOLD -> step.getBindingId();
                case RUN_COMMAND -> step.getActionId();
                case DELAY -> "";
            };
        }

        private static String durationValue(MacroStep step) {
            return step.getType() == MacroStep.Type.BINDING_HOLD || step.getType() == MacroStep.Type.DELAY
                    ? Integer.toString(step.getDurationMs())
                    : "";
        }
    }
}

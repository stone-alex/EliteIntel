package elite.intel.ui.view;

import elite.intel.ai.brain.actions.macro.MacroDefinition;
import elite.intel.ai.brain.actions.macro.MacroEditorValidator;
import elite.intel.ai.brain.actions.macro.MacroParameterSpec;
import elite.intel.ai.brain.actions.macro.MacroStep;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
    private final ParamsTableModel paramsModel = new ParamsTableModel();
    private final JTable paramsTable = new JTable(paramsModel);
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
        paramsModel.setParameters(macro.getParameters());
        stepsModel.setSteps(macro.getSteps());
    }

    private void buildUi(boolean existing) {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(AppTheme.BG);
        content.setBorder(new EmptyBorder(16, 18, 12, 18));
        content.add(form(existing), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(paramsPanel(), BorderLayout.NORTH);
        center.add(stepsPanel(), BorderLayout.CENTER);
        content.add(center, BorderLayout.CENTER);

        content.add(bottomPanel(), BorderLayout.SOUTH);
        setContentPane(content);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(860, 860));
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

    private JPanel paramsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel label = new JLabel(getText("actions.macros.editor.parameters"));
        label.setForeground(AppTheme.FG_MUTED);
        panel.add(label, BorderLayout.NORTH);

        paramsTable.setFillsViewportHeight(true);
        paramsTable.setRowHeight(26);
        paramsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paramsTable.setBackground(AppTheme.BG_PANEL);
        paramsTable.setForeground(AppTheme.FG);
        paramsTable.setSelectionBackground(AppTheme.SEL_BG);
        paramsTable.setSelectionForeground(AppTheme.SEL_FG);
        paramsTable.getTableHeader().setBackground(AppTheme.BG);
        paramsTable.getTableHeader().setForeground(AppTheme.FG);
        paramsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editParam();
            }
        });
        JScrollPane scroll = new JScrollPane(paramsTable);
        scroll.setPreferredSize(new Dimension(0, 130));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        addStepButton(buttons, "actions.macros.editor.param.add", this::addParam);
        addStepButton(buttons, "actions.macros.editor.param.edit", this::editParam);
        addStepButton(buttons, "actions.macros.editor.param.remove", this::removeParam);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void addParam() {
        MacroParameterSpec spec = new MacroParamSpecEditorDialog(this, null).showDialog();
        if (spec != null) {
            paramsModel.addParameter(spec);
        }
    }

    private void editParam() {
        int row = selectedParamRow();
        if (row < 0) return;
        MacroParameterSpec edited = new MacroParamSpecEditorDialog(this, paramsModel.getParameter(row)).showDialog();
        if (edited != null) {
            paramsModel.setParameter(row, edited);
        }
    }

    private void removeParam() {
        int row = selectedParamRow();
        if (row >= 0) {
            paramsModel.removeParameter(row);
        }
    }

    private int selectedParamRow() {
        int viewRow = paramsTable.getSelectedRow();
        return viewRow < 0 ? -1 : paramsTable.convertRowIndexToModel(viewRow);
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
        stepsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editStep();
            }
        });
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
        MacroStep step = new MacroStepEditorDialog(
                this,
                null,
                paramsModel.parameters(),
                this::addMissingMacroParameters
        ).showDialog();
        if (step != null) {
            stepsModel.addStep(step);
        }
    }

    private void editStep() {
        int row = selectedStepRow();
        if (row < 0) {
            return;
        }
        MacroStep edited = new MacroStepEditorDialog(
                this,
                stepsModel.getStep(row),
                paramsModel.parameters(),
                this::addMissingMacroParameters
        ).showDialog();
        if (edited != null) {
            stepsModel.setStep(row, edited);
        }
    }

    private void addMissingMacroParameters(List<MacroParameterSpec> specs) {
        if (specs == null || specs.isEmpty()) {
            return;
        }
        for (MacroParameterSpec spec : specs) {
            String name = spec == null ? null : spec.getName();
            if (name == null || name.isBlank() || paramsModel.hasParameter(name)) {
                continue;
            }
            paramsModel.addParameter(spec);
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
                paramsModel.parameters(),
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
        installPlainTextPaste(area);
        return area;
    }

    private static void installPlainTextPaste(JTextComponent component) {
        Action paste = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                pastePlainText(component);
            }
        };
        component.getActionMap().put(DefaultEditorKit.pasteAction, paste);
        component.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                DefaultEditorKit.pasteAction
        );
        component.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_DOWN_MASK),
                DefaultEditorKit.pasteAction
        );
    }

    private static void pastePlainText(JTextComponent component) {
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(new ClipboardFlavorNoiseFilter(originalErr), true));
        try {
            Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (contents == null) {
                return;
            }
            Object data = contents.getTransferData(DataFlavor.stringFlavor);
            if (data instanceof String text) {
                component.replaceSelection(text);
            }
        } catch (UnsupportedFlavorException | IOException | IllegalStateException e) {
            Toolkit.getDefaultToolkit().beep();
        } finally {
            System.setErr(originalErr);
        }
    }

    private static final class ClipboardFlavorNoiseFilter extends OutputStream {
        private final PrintStream delegate;
        private final StringBuilder line = new StringBuilder();

        private ClipboardFlavorNoiseFilter(PrintStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public synchronized void write(int b) {
            char c = (char) b;
            line.append(c);
            if (c == '\n') {
                flushLine();
            }
        }

        @Override
        public synchronized void flush() {
            if (!line.isEmpty()) {
                flushLine();
            }
            delegate.flush();
        }

        private void flushLine() {
            String text = line.toString();
            line.setLength(0);
            if (!isIntelliJClipboardFlavorNoise(text)) {
                delegate.print(text);
            }
        }

        private static boolean isIntelliJClipboardFlavorNoise(String text) {
            return text.contains("while constructing DataFlavor")
                    && (text.contains("com/intellij/openapi/editor/RawText")
                    || text.contains("com/intellij/codeInsight/editorActions/FoldingData")
                    || text.contains("com/intellij/openapi/editor/impl/EditorCopyPasteHelperImpl$CopyPasteOptionsTransferableData"));
        }
    }

    private static final class ParamsTableModel extends AbstractTableModel {
        private final List<MacroParameterSpec> params = new ArrayList<>();
        private final String[] columns = {
                getText("actions.macros.editor.param.column.name"),
                getText("actions.macros.editor.param.column.type"),
                getText("actions.macros.editor.param.column.required"),
                getText("actions.macros.editor.param.column.description"),
                getText("actions.macros.editor.param.column.examples")
        };

        void setParameters(List<MacroParameterSpec> newParams) {
            params.clear();
            if (newParams != null) params.addAll(newParams);
            fireTableDataChanged();
        }

        List<MacroParameterSpec> parameters() { return List.copyOf(params); }

        MacroParameterSpec getParameter(int row) { return params.get(row); }

        void addParameter(MacroParameterSpec spec) {
            params.add(spec);
            fireTableRowsInserted(params.size() - 1, params.size() - 1);
        }

        void setParameter(int row, MacroParameterSpec spec) {
            params.set(row, spec);
            fireTableRowsUpdated(row, row);
        }

        void removeParameter(int row) {
            params.remove(row);
            fireTableRowsDeleted(row, row);
        }

        boolean hasParameter(String name) {
            return params.stream().anyMatch(param -> param.getName().equalsIgnoreCase(name));
        }

        @Override public int getRowCount() { return params.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            MacroParameterSpec spec = params.get(row);
            return switch (col) {
                case 0 -> spec.getName();
                case 1 -> spec.getType();
                case 2 -> spec.isRequired() ? "✓" : "";
                case 3 -> spec.getDescription();
                case 4 -> String.join(", ", spec.getExamples());
                default -> "";
            };
        }
    }

    private static final class StepsTableModel extends AbstractTableModel {
        private final List<MacroStep> steps = new ArrayList<>();
        private final String[] columns = {
                getText("actions.macros.editor.step.type"),
                getText("actions.macros.editor.step.column.value"),
                getText("actions.macros.editor.step.durationMs")
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
                case 0 -> MacroStepEditorDialog.stepTypeLabel(step.getType());
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
                case RAW_KEY -> new BindingSlotDisplayFormatter().formatRawKeyStep(step.getRawKey(), step.getRawKeyModifier());
            };
        }

        private static String durationValue(MacroStep step) {
            return switch (step.getType()) {
                case BINDING_HOLD, DELAY, RAW_KEY -> Integer.toString(step.getDurationMs());
                default -> "";
            };
        }
    }
}

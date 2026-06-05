package elite.intel.ui.view;

import elite.intel.ai.brain.actions.macro.MacroParameterSpec;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Modal editor for one {@link MacroParameterSpec}.
 */
final class MacroParamSpecEditorDialog extends JDialog {

    private static final String[] TYPES = {"string", "number", "boolean"};

    private final JTextField nameField = new JTextField(28);
    private final JComboBox<String> typeCombo = new JComboBox<>(TYPES);
    private final JCheckBox requiredCheck = new JCheckBox();
    private final JTextField descriptionField = new JTextField(28);
    private final JTextField examplesField = new JTextField(28);
    private final JTextField extractionHintField = new JTextField(28);
    private MacroParameterSpec result;

    MacroParamSpecEditorDialog(Component parent, MacroParameterSpec spec) {
        super(SwingUtilities.getWindowAncestor(parent),
                getText("actions.macros.editor.param.title"),
                ModalityType.APPLICATION_MODAL);
        populate(spec);
        buildUi();
    }

    MacroParameterSpec showDialog() {
        setVisible(true);
        return result;
    }

    private void populate(MacroParameterSpec spec) {
        if (spec == null) return;
        nameField.setText(spec.getName() != null ? spec.getName() : "");
        typeCombo.setSelectedItem(spec.getType() != null ? spec.getType() : "string");
        requiredCheck.setSelected(spec.isRequired());
        descriptionField.setText(spec.getDescription());
        examplesField.setText(String.join(", ", spec.getExamples()));
        extractionHintField.setText(spec.getExtractionHint() != null ? spec.getExtractionHint() : "");
    }

    private void buildUi() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(AppTheme.BG);
        content.setBorder(new EmptyBorder(16, 18, 12, 18));
        content.add(form(), BorderLayout.CENTER);
        content.add(buttons(), BorderLayout.SOUTH);
        setContentPane(content);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(480, 320));
        setLocationRelativeTo(getOwner());
    }

    private JPanel form() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG);
        GridBagConstraints gbc = AppTheme.baseGbc();

        addRow(panel, gbc, getText("actions.macros.editor.param.name"), nameField);
        addRow(panel, gbc, getText("actions.macros.editor.param.type"), typeCombo);
        addRow(panel, gbc, getText("actions.macros.editor.param.required"), requiredCheck);
        addRow(panel, gbc, getText("actions.macros.editor.param.description"), descriptionField);
        addRow(panel, gbc, getText("actions.macros.editor.param.examples"), examplesField);
        addRow(panel, gbc, getText("actions.macros.editor.param.extractionHint"), extractionHintField);

        AppTheme.applyDarkPalette(panel);
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(labelText);
        label.setForeground(AppTheme.FG_MUTED);
        label.setBorder(new EmptyBorder(0, 0, 0, 12));
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
        save.addActionListener(e -> save());
        panel.add(save, BorderLayout.WEST);
        JButton cancel = AppTheme.makeButtonSubtle(getText("button.cancel"));
        cancel.addActionListener(e -> dispose());
        panel.add(cancel, BorderLayout.EAST);
        getRootPane().setDefaultButton(save);
        return panel;
    }

    private void save() {
        String name = nameField.getText().trim();
        if (name.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    getText("actions.macros.editor.param.name") + " is required.",
                    getText("actions.macros.editor.validation.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String type = (String) typeCombo.getSelectedItem();
        if (type == null) type = "string";
        boolean required = requiredCheck.isSelected();
        String description = descriptionField.getText().trim();
        List<String> examples = Arrays.stream(examplesField.getText().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        String hint = extractionHintField.getText().trim();

        result = new MacroParameterSpec(name, type, required, description,
                examples.isEmpty() ? null : examples,
                hint.isBlank() ? null : hint);
        dispose();
    }
}

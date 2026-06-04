package elite.intel.ui.view;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.AiActionsMap;
import elite.intel.ai.brain.actions.catalog.CommandCatalogEntry;
import elite.intel.ai.brain.actions.catalog.CommandCatalogEntryType;
import elite.intel.ai.brain.commons.ResponseRouter;
import elite.intel.ai.brain.i18n.AiActionLocalizations;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Displays command metadata and provides a GUI entry point into post-LLM command dispatch.
 */
public final class CommandDetailsDialog extends JDialog {

    private static final Pattern PARAMETER_BLOCK = Pattern.compile("\\{([^}]+)}");
    private static final Color RUN_BUTTON_BG = new Color(0x1F8F3A);
    private static final int GUI_COMMAND_DISPATCH_DELAY_MS = 350;
    private final CommandCatalogEntry entry;

    public CommandDetailsDialog(Component parent, CommandCatalogEntry entry) {
        super(SwingUtilities.getWindowAncestor(parent), getText("actions.commands.details.title"), ModalityType.APPLICATION_MODAL);
        this.entry = entry;
        buildUi();
    }

    public void showDialog() {
        setVisible(true);
    }

    private void buildUi() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(AppTheme.BG);
        content.setBorder(new EmptyBorder(16, 18, 12, 18));
        content.add(header(), BorderLayout.NORTH);
        content.add(details(), BorderLayout.CENTER);
        content.add(buttons(), BorderLayout.SOUTH);
        setContentPane(content);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(720, 520));
        setLocationRelativeTo(getOwner());
    }

    private JPanel header() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel name = new JLabel(entry.name());
        name.setForeground(AppTheme.FG);
        name.setFont(name.getFont().deriveFont(Font.BOLD, name.getFont().getSize2D() + 5f));

        JLabel id = new JLabel(entry.id());
        id.setForeground(AppTheme.FG_MUTED);
        id.setBorder(new EmptyBorder(4, 0, 0, 0));

        panel.add(name);
        panel.add(id);
        return panel;
    }

    private JPanel details() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0, 5, 12);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        addLabelValue(panel, gbc, getText("actions.commands.details.name"), entry.name());
        addLabelValue(panel, gbc, getText("actions.commands.details.id"), entry.id());
        addLabelValue(panel, gbc, getText("actions.commands.details.type"), readableType(entry.type()));
        addDescription(panel, gbc);
        addPhrases(panel, gbc);

        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), gbc);
        return panel;
    }

    private void addLabelValue(JPanel panel, GridBagConstraints gbc, String labelText, String value) {
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = detailLabel(labelText);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(AppTheme.FG);
        panel.add(valueLabel, gbc);
        gbc.gridy++;
    }

    private void addDescription(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(detailLabel(getText("actions.commands.details.description")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextArea description = readOnlyTextArea(entry.description());
        description.setRows(3);
        panel.add(description, gbc);
        gbc.gridy++;
    }

    private void addPhrases(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(detailLabel(getText("actions.commands.details.phrases")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(phrasesSection(), gbc);
        gbc.gridy++;
        gbc.weighty = 0.0;
    }

    private JComponent phrasesSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(phrasesComponent(), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        JButton suggestCorrection = AppTheme.makeButtonSubtle(getText("actions.commands.details.suggestCorrection"));
        suggestCorrection.addActionListener(event -> openPhraseCorrectionDialog());
        actions.add(suggestCorrection);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent phrasesComponent() {
        List<String> phrases = currentPhrases();
        if (phrases.isEmpty()) {
            JLabel empty = new JLabel(getText("actions.commands.details.noPhrases"));
            empty.setForeground(AppTheme.FG_MUTED);
            empty.setBorder(new EmptyBorder(8, 8, 8, 8));
            return empty;
        }

        JTextArea area = readOnlyTextArea(String.join(System.lineSeparator(), phrases));
        area.setRows(Math.min(10, Math.max(4, phrases.size())));
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.getViewport().setBackground(AppTheme.BG_PANEL);
        return scrollPane;
    }

    private JTextArea readOnlyTextArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(AppTheme.BG_PANEL);
        area.setForeground(AppTheme.FG);
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        return area;
    }

    private JLabel detailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(AppTheme.FG_MUTED);
        label.setMinimumSize(new Dimension(220, 24));
        label.setPreferredSize(new Dimension(220, 24));
        return label;
    }

    private JPanel buttons() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftButtons.setOpaque(false);

        JButton run = runButton();
        run.addActionListener(event -> runCommand());
        leftButtons.add(run);

        panel.add(leftButtons, BorderLayout.WEST);

        JButton close = AppTheme.makeButton(getText("actions.commands.details.close"));
        close.addActionListener(event -> dispose());
        panel.add(close, BorderLayout.EAST);
        getRootPane().setDefaultButton(close);
        return panel;
    }

    private JButton runButton() {
        JButton button = new JButton(getText("actions.commands.details.run"), new PlayIcon()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = RUN_BUTTON_BG;
                    ButtonModel model = getModel();
                    if (model.isPressed()) {
                        base = base.darker();
                    } else if (model.isRollover()) {
                        base = base.brighter();
                    }
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(graphics);
            }
        };
        AppTheme.styleButton(button);
        button.setBackground(RUN_BUTTON_BG);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RUN_BUTTON_BG, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        button.setIconTextGap(8);
        return button;
    }

    private void runCommand() {
        JsonObject params = promptForParams();
        if (params == null) {
            return;
        }

        Window owner = getOwner();
        dispose();
        if (owner != null) {
            owner.toBack();
        }

        // Keyboard-backed commands target the active OS window; give focus a moment to leave this dialog.
        Timer dispatchTimer = new Timer(
                GUI_COMMAND_DISPATCH_DELAY_MS,
                event -> ResponseRouter.getInstance().executeCommandFromGUI(entry.id(), params)
        );
        dispatchTimer.setRepeats(false);
        dispatchTimer.start();
    }

    private void openPhraseCorrectionDialog() {
        new PhraseCorrectionSuggestionDialog(this, entry, currentPhrases()).showDialog();
    }

    private List<String> currentPhrases() {
        return AiActionLocalizations.phrasesForAction(entry.id());
    }

    private JsonObject promptForParams() {
        List<CommandParameter> parameters = commandParameters();
        JsonObject params = new JsonObject();
        if (parameters.isEmpty()) {
            return params;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG);
        Map<CommandParameter, JComponent> fields = new LinkedHashMap<>();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (CommandParameter parameter : parameters) {
            gbc.gridx = 0;
            gbc.weightx = 0.0;
            JLabel label = new JLabel(parameter.name());
            label.setForeground(AppTheme.FG);
            panel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            JComponent field = parameter.isBoolean()
                    ? new JComboBox<>(new String[]{"", "true", "false"})
                    : new JTextField(24);
            field.setToolTipText(parameter.hint());
            AppTheme.applyDarkPalette(field);
            panel.add(field, gbc);
            fields.put(parameter, field);
            gbc.gridy++;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                getText("actions.commands.details.parameters.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        for (Map.Entry<CommandParameter, JComponent> entry : fields.entrySet()) {
            CommandParameter parameter = entry.getKey();
            JComponent field = entry.getValue();
            if (field instanceof JComboBox<?> comboBox) {
                Object selected = comboBox.getSelectedItem();
                if (selected != null && !selected.toString().isBlank()) {
                    params.addProperty(parameter.name(), Boolean.parseBoolean(selected.toString()));
                }
            } else if (field instanceof JTextField textField) {
                String value = textField.getText().trim();
                if (!value.isBlank()) {
                    params.addProperty(parameter.name(), value);
                }
            }
        }
        return params;
    }

    private List<CommandParameter> commandParameters() {
        Map<String, CommandParameter> parameters = new LinkedHashMap<>();
        AiActionsMap.getInstance().actionMap(true).forEach((phraseGroup, action) -> {
            if (!entry.id().equalsIgnoreCase(action)) {
                return;
            }

            // Command aliases are the existing source of truth for LLM parameter names.
            Matcher matcher = PARAMETER_BLOCK.matcher(phraseGroup);
            while (matcher.find()) {
                for (String rawParameter : matcher.group(1).split(",")) {
                    CommandParameter parameter = CommandParameter.from(rawParameter);
                    if (parameter != null) {
                        parameters.putIfAbsent(parameter.name(), parameter);
                    }
                }
            }
        });
        return List.copyOf(parameters.values());
    }

    private String readableType(CommandCatalogEntryType type) {
        return switch (type) {
            case BUILT_IN_BINDING -> getText("actions.commands.type.builtInBinding");
            case BUILT_IN_ACTION -> getText("actions.commands.type.builtInAction");
            case USER_MACRO -> getText("actions.commands.type.userMacro");
        };
    }

    private record CommandParameter(String name, String hint) {
        private boolean isBoolean() {
            return "true/false".equalsIgnoreCase(hint);
        }

        private static CommandParameter from(String rawParameter) {
            if (rawParameter == null || rawParameter.isBlank()) {
                return null;
            }
            String[] parts = rawParameter.trim().split(":", 2);
            String name = parts[0].trim();
            if (name.isBlank()) {
                return null;
            }
            String hint = parts.length > 1 ? parts[1].trim() : "X";
            return new CommandParameter(name, hint);
        }
    }

    private static final class PlayIcon implements Icon {
        private static final int WIDTH = 11;
        private static final int HEIGHT = 12;

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BUTTON_FG);
                Polygon triangle = new Polygon(
                        new int[]{x, x, x + WIDTH},
                        new int[]{y, y + HEIGHT, y + HEIGHT / 2},
                        3
                );
                g2.fill(triangle);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return WIDTH;
        }

        @Override
        public int getIconHeight() {
            return HEIGHT;
        }
    }
}

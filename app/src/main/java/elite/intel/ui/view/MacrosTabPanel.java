package elite.intel.ui.view;

import elite.intel.ai.brain.actions.catalog.CommandCatalog;
import elite.intel.ai.brain.actions.catalog.CommandCatalogEntry;
import elite.intel.ai.brain.actions.handlers.CommandHandlerFactory;
import elite.intel.ai.brain.actions.macro.MacroDefinition;
import elite.intel.ai.brain.actions.macro.MacroRepository;
import elite.intel.ai.brain.actions.macro.MacroRegistry;
import elite.intel.ai.brain.actions.macro.MacroStep;
import elite.intel.ai.brain.i18n.AiActionLocalizations;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Read-only macro catalog view backed by the loaded macro registry.
 */
public class MacrosTabPanel extends JPanel {

    private final CommandCatalog commandCatalog = new CommandCatalog();
    private final MacroRepository macroRepository = new MacroRepository();
    private List<MacroRow> visibleRows = List.of();
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;

    public MacrosTabPanel() {
        buildUi();
        initData();
    }

    private void buildUi() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(AppTheme.BG);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(headerPanel(), BorderLayout.NORTH);
        JPanel controls = new JPanel(new BorderLayout(8, 0));
        controls.setOpaque(false);
        controls.add(searchPanel(), BorderLayout.CENTER);
        controls.add(actionPanel(), BorderLayout.EAST);
        top.add(controls, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        tableModel = new ReadOnlyTableModel(columnNames(), 0);
        table = new JTable(tableModel);
        styleTable(table);
        table.addMouseListener(new MacroTableMouseListener());
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "openMacroDetails");
        table.getActionMap().put("openMacroDetails", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                openMacroDetailsAtViewRow(table.getSelectedRow());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(AppTheme.BG_PANEL);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void initData() {
        tableModel.setRowCount(0);
        visibleRows = filteredRows();
        for (MacroRow row : visibleRows) {
            tableModel.addRow(new Object[]{
                    new MacroNameCell(row.entry().name(), row.entry().id()),
                    row.phrasesText()
            });
        }
    }

    private JPanel headerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel(getText("actions.macros.title"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 4f));
        title.setForeground(AppTheme.FG);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        return panel;
    }

    private JPanel actionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        addActionButton(panel, "actions.macros.action.new", this::newMacro);
        return panel;
    }

    private void addActionButton(JPanel panel, String key, Runnable action) {
        JButton button = AppTheme.makeButtonSubtle(getText(key));
        button.addActionListener(event -> action.run());
        panel.add(button);
    }

    private JPanel searchPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        JLabel label = new JLabel(getText("actions.commands.search.label"));
        label.setForeground(AppTheme.FG);
        panel.add(label, BorderLayout.WEST);

        searchField = new PlaceholderTextField(getText("actions.macros.search.placeholder"));
        searchField.getDocument().addDocumentListener(new SearchDocumentListener());
        panel.add(searchField, BorderLayout.CENTER);

        JButton clearButton = new JButton("X");
        clearButton.setToolTipText(getText("actions.commands.search.clearTooltip"));
        clearButton.setMargin(new Insets(2, 8, 2, 8));
        clearButton.setFocusable(false);
        clearButton.addActionListener(event -> resetSearch());
        panel.add(clearButton, BorderLayout.EAST);

        return panel;
    }

    private String[] columnNames() {
        return new String[]{
                getText("actions.macros.column.name"),
                getText("actions.macros.column.phrases")
        };
    }

    private List<MacroRow> sortedRows() {
        List<MacroDefinition> macros = MacroRegistry.getInstance().getMacros();
        Map<String, MacroDefinition> macrosById = macros.stream()
                .collect(Collectors.toMap(
                        MacroDefinition::getId,
                        Function.identity(),
                        (first, second) -> second,
                        LinkedHashMap::new
                ));

        return commandCatalog.entries(macros).stream()
                .filter(CommandCatalogEntry::isMacro)
                .map(entry -> new MacroRow(entry, macrosById.get(entry.id())))
                .filter(row -> row.macro() != null)
                .sorted(Comparator
                        .comparing((MacroRow row) -> row.entry().name(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(row -> row.entry().id()))
                .toList();
    }

    private List<MacroRow> filteredRows() {
        String query = normalizedSearchText();
        return sortedRows().stream()
                .filter(row -> query.isBlank() || matchesSearch(row, query))
                .toList();
    }

    private boolean matchesSearch(MacroRow row, String query) {
        return row.entry().name().toLowerCase(Locale.ROOT).contains(query)
                || row.entry().id().toLowerCase(Locale.ROOT).contains(query)
                || row.entry().description().toLowerCase(Locale.ROOT).contains(query)
                || row.phrasesText().toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalizedSearchText() {
        if (searchField == null) {
            return "";
        }
        return searchField.getText().trim().toLowerCase(Locale.ROOT);
    }

    private void resetSearch() {
        searchField.setText("");
        initData();
    }

    private void openMacroDetailsAtViewRow(int viewRow) {
        if (viewRow < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= visibleRows.size()) {
            return;
        }

        MacroRow row = visibleRows.get(modelRow);
        new CommandDetailsDialog(
                this,
                row.entry(),
                row.phrases(),
                false,
                row.sequenceText(),
                () -> editMacro(row),
                () -> deleteMacro(row)
        ).showDialog();
    }

    private void newMacro() {
        MacroDefinition saved = new MacroEditorDialog(this, null, MacroRegistry.getInstance().getMacros()).showDialog();
        if (saved == null) {
            return;
        }
        List<MacroDefinition> macros = new java.util.ArrayList<>(MacroRegistry.getInstance().getMacros());
        macros.add(saved);
        persistAndRefresh(macros);
    }

    private void editMacro(MacroRow row) {
        MacroDefinition saved = new MacroEditorDialog(this, row.macro(), MacroRegistry.getInstance().getMacros()).showDialog();
        if (saved == null) {
            return;
        }
        List<MacroDefinition> macros = MacroRegistry.getInstance().getMacros().stream()
                .map(macro -> macro.getId().equalsIgnoreCase(row.macro().getId()) ? saved : macro)
                .toList();
        persistAndRefresh(macros);
    }

    private void deleteMacro(MacroRow row) {
        int result = JOptionPane.showConfirmDialog(
                this,
                getText("actions.macros.delete.confirm", row.entry().name()),
                getText("actions.macros.delete.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        List<MacroDefinition> macros = MacroRegistry.getInstance().getMacros().stream()
                .filter(macro -> !macro.getId().equalsIgnoreCase(row.macro().getId()))
                .toList();
        persistAndRefresh(macros);
    }

    private void persistAndRefresh(List<MacroDefinition> macros) {
        if (!macroRepository.trySave(macros)) {
            JOptionPane.showMessageDialog(
                    this,
                    getText("actions.macros.save.error"),
                    getText("actions.macros.title"),
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        MacroRegistry.getInstance().replaceMacros(macros);
        CommandHandlerFactory.getInstance().refreshMacroHandlers();
        initData();
    }

    private void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(56);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setBackground(AppTheme.BG_PANEL);
        table.setForeground(AppTheme.FG);
        table.setGridColor(AppTheme.BG);
        table.setSelectionBackground(AppTheme.SEL_BG);
        table.setSelectionForeground(AppTheme.SEL_FG);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.getTableHeader().setBackground(AppTheme.BG);
        table.getTableHeader().setForeground(AppTheme.FG);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.setDefaultRenderer(Object.class, new CellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(520);
    }

    private record MacroRow(CommandCatalogEntry entry, MacroDefinition macro) {
        private List<String> phrases() {
            return AiActionLocalizations.splitPhraseGroup(macro.getPhrases());
        }

        private String phrasesText() {
            return String.join(", ", phrases());
        }

        private String sequenceText() {
            if (macro.getSteps() == null || macro.getSteps().isEmpty()) {
                return "";
            }

            StringBuilder text = new StringBuilder();
            for (int i = 0; i < macro.getSteps().size(); i++) {
                if (!text.isEmpty()) {
                    text.append(System.lineSeparator());
                }
                text.append(i + 1)
                        .append(". ")
                        .append(stepText(macro.getSteps().get(i)));
            }
            return text.toString();
        }

        private static String stepText(MacroStep step) {
            if (step == null || step.getType() == null) {
                return "INVALID_STEP";
            }
            String label = MacroStepEditorDialog.stepTypeLabel(step.getType()) + ": ";
            return switch (step.getType()) {
                case SPEAK        -> label + "\"" + step.getText() + "\"";
                case DELAY        -> label + step.getDurationMs() + " ms";
                case BINDING_TAP  -> label + step.getBindingId();
                case BINDING_HOLD -> label + step.getBindingId() + " (" + step.getDurationMs() + " ms)";
                case RUN_COMMAND  -> label + step.getActionId();
                case RAW_KEY -> {
                    String combo = new BindingSlotDisplayFormatter().formatRawKeyStep(step.getRawKey(), step.getRawKeyModifier());
                    yield label + combo + (step.getDurationMs() > 0 ? " (" + step.getDurationMs() + " ms)" : "");
                }
            };
        }

    }

    private static final class ReadOnlyTableModel extends DefaultTableModel {
        private ReadOnlyTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        private HeaderRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setBackground(AppTheme.BG);
            label.setForeground(AppTheme.FG);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setBorder(new EmptyBorder(6, 8, 6, 8));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            return label;
        }
    }

    private static final class CellRenderer extends DefaultTableCellRenderer {
        private CellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof MacroNameCell macroNameCell) {
                label.setText(macroNameCell.toHtml());
                label.setToolTipText(macroNameCell.toString());
            } else {
                String text = String.valueOf(value);
                label.setText(text);
                label.setToolTipText(text);
            }
            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? AppTheme.BG_PANEL : AppTheme.LOG_BG);
                label.setForeground(AppTheme.FG);
            } else {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            }
            label.setBorder(new EmptyBorder(4, 8, 4, 8));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            return label;
        }
    }

    private record MacroNameCell(String name, String id) {
        private String toHtml() {
            return "<html><div>"
                    + escapeHtml(name)
                    + "<br><span style='font-size:10px;color:#8f96a3;'>"
                    + escapeHtml(id)
                    + "</span></div></html>";
        }

        @Override
        public String toString() {
            return name + " " + id;
        }
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static final class PlaceholderTextField extends JTextField {
        private final String placeholder;

        private PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setToolTipText(placeholder);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (!getText().isEmpty() || placeholder == null || placeholder.isBlank()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setColor(AppTheme.FG_MUTED);
                FontMetrics metrics = g2.getFontMetrics();
                Insets insets = getInsets();
                int x = insets.left + 2;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g2.drawString(placeholder, x, y);
            } finally {
                g2.dispose();
            }
        }
    }

    private final class SearchDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            initData();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            initData();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            initData();
        }
    }

    private final class MacroTableMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent event) {
            if (!SwingUtilities.isLeftMouseButton(event)) {
                return;
            }
            int row = table.rowAtPoint(event.getPoint());
            if (row < 0) {
                return;
            }
            table.setRowSelectionInterval(row, row);
            table.requestFocusInWindow();
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            if (!SwingUtilities.isLeftMouseButton(event)) {
                return;
            }
            int row = table.rowAtPoint(event.getPoint());
            if (row >= 0) {
                openMacroDetailsAtViewRow(row);
            }
        }
    }
}

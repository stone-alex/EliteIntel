package elite.intel.ui.view;

import elite.intel.ai.brain.actions.catalog.CommandCatalog;
import elite.intel.ai.brain.actions.catalog.CommandCatalogEntry;
import elite.intel.ai.brain.actions.catalog.CommandCatalogEntryType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Read-only command catalog view used to inspect and launch built-in commands.
 */
public class CommandCatalogTablePanel extends JPanel {

    private final CommandCatalog commandCatalog = new CommandCatalog();
    private List<CommandCatalogEntry> visibleEntries = List.of();
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;

    public CommandCatalogTablePanel() {
        buildUi();
        initData();
    }

    private void buildUi() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(AppTheme.HUD_PADDING, AppTheme.HUD_PADDING, AppTheme.HUD_PADDING, AppTheme.HUD_PADDING));
        setBackground(AppTheme.HUD_BG);

        tableModel = new ReadOnlyTableModel(columnNames(), 0);
        table = new JTable(tableModel);
        styleTable(table);
        table.addMouseListener(new CommandDetailsMouseListener());

        JScrollPane scrollPane = HudTable.scrollPane(table);

        HudSection catalogSection = new HudSection(getText("actions.commands.section.catalog"), new BorderLayout(AppTheme.HUD_GAP, AppTheme.HUD_GAP));
        catalogSection.body().add(searchPanel(), BorderLayout.NORTH);
        catalogSection.body().add(scrollPane, BorderLayout.CENTER);
        add(catalogSection, BorderLayout.CENTER);
    }

    public void initData() {
        tableModel.setRowCount(0);
        visibleEntries = filteredEntries();
        for (CommandCatalogEntry entry : visibleEntries) {
            tableModel.addRow(new Object[]{
                    new CommandNameCell(entry.name(), entry.id()),
                    readableType(entry.type())
            });
        }
    }

    private JPanel searchPanel() {
        JPanel panel = AppTheme.transparentPanel(new BorderLayout(AppTheme.HUD_GAP, 0));

        JLabel label = new JLabel(getText("actions.commands.search.label"));
        label.setForeground(AppTheme.FG);
        panel.add(label, BorderLayout.WEST);

        HudSearchField hudSearchField = new HudSearchField(
                getText("actions.commands.search.placeholder"),
                getText("actions.commands.search.clearTooltip"));
        searchField = hudSearchField.textField();
        searchField.getDocument().addDocumentListener(new SearchDocumentListener());
        panel.add(hudSearchField, BorderLayout.CENTER);

        return panel;
    }

    private String[] columnNames() {
        return new String[]{
                getText("actions.commands.column.name"),
                getText("actions.commands.column.type")
        };
    }

    private List<CommandCatalogEntry> sortedEntries() {
        return commandCatalog.entries().stream()
                .sorted(Comparator
                        .comparing(CommandCatalogEntry::name, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(CommandCatalogEntry::id))
                .toList();
    }

    private List<CommandCatalogEntry> filteredEntries() {
        String query = normalizedSearchText();
        return sortedEntries().stream()
                .filter(entry -> query.isBlank() || matchesSearch(entry, query))
                .toList();
    }

    private boolean matchesSearch(CommandCatalogEntry entry, String query) {
        return entry.name().toLowerCase(Locale.ROOT).contains(query)
                || entry.id().toLowerCase(Locale.ROOT).contains(query);
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

    private void openCommandDetailsAtViewRow(int viewRow) {
        if (viewRow < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= visibleEntries.size()) {
            return;
        }
        new CommandDetailsDialog(this, visibleEntries.get(modelRow)).showDialog();
    }

    private String readableType(CommandCatalogEntryType type) {
        return switch (type) {
            case BUILT_IN_BINDING -> getText("actions.commands.type.builtInBinding");
            case BUILT_IN_ACTION -> getText("actions.commands.type.builtInAction");
            case CUSTOM_COMMAND -> getText("actions.commands.type.customCommand");
        };
    }

    private void styleTable(JTable table) {
        HudTable.style(table);
        table.setRowHeight(48);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new CellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
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

    private static final class CellRenderer extends HudTable.CellRenderer {
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
            if (value instanceof CommandNameCell commandNameCell) {
                label.setText(commandNameCell.toHtml());
            }
            return label;
        }
    }

    private record CommandNameCell(String name, String id) {
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

    private final class CommandDetailsMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (!SwingUtilities.isLeftMouseButton(event)) {
                return;
            }
            int row = table.rowAtPoint(event.getPoint());
            if (row >= 0) {
                openCommandDetailsAtViewRow(row);
            }
        }
    }
}

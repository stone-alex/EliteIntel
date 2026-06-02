package elite.intel.ui.view;

import elite.intel.ai.hands.BindingGroup;
import elite.intel.ai.hands.BindingGroupClassifier;
import elite.intel.ai.hands.BindingsLoader;
import elite.intel.ai.hands.BindingsMonitor;
import elite.intel.ai.hands.KeyBindingsParser;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.BindingsUpdatedEvent;
import com.google.common.eventbus.Subscribe;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;
import static elite.intel.ui.view.AppTheme.*;

public class BindingsTabPanel extends JPanel {

    private static final int TABLE_ROW_HEIGHT = 28;
    private static final int SCROLL_UNIT_ROWS = 2;
    private static final Border TABLE_SECTION_BORDER = BorderFactory.createMatteBorder(1, 0, 0, 0, ACCENT);
    private static final Border TABLE_HEADER_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, BUTTON_BG);

    private final BindingsLoader loader = new BindingsLoader();
    private final KeyBindingsParser parser = KeyBindingsParser.getInstance();
    private final BindingsMonitor monitor = BindingsMonitor.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();

    private JTextField profileField;
    private JTextField filePathField;
    private JTextField bindingsDirField;
    private JPanel usedBindingsPanel;
    private JPanel missingBindingsPanel;
    private JScrollPane usedBindingsScrollPane;
    private JScrollPane missingBindingsScrollPane;
    private JTabbedPane tabs;

    public BindingsTabPanel() {
        buildUi();
        EventBusManager.register(this);
    }

    @Subscribe
    public void onBindingsUpdated(BindingsUpdatedEvent event) {
        SwingUtilities.invokeLater(this::initData);
    }

    private void buildUi() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BG);

        JPanel details = new JPanel(new GridBagLayout());
        details.setOpaque(false);
        GridBagConstraints gbc = baseGbc();

        // Bindings Directory
        addLabel(details, getText("player.bindingsDirectory"), gbc);
        bindingsDirField = readOnlyField();
        bindingsDirField.setToolTipText(getText("player.bindingsDirectory.tooltip"));
        addField(details, bindingsDirField, gbc, 1, 0.8);
        JButton selectBindingsDirButton = makeButton(getText("button.select"));
        selectBindingsDirButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle(getText("player.bindingsDirectory.dialog"));
            String current = playerSession.getBindingsDir().toString();
            if (!current.isBlank())
                chooser.setCurrentDirectory(new File(current).getParentFile());
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                playerSession.setBindingsDir(path);
                bindingsDirField.setText(path);
                initData();
            }
        });
        addField(details, selectBindingsDirButton, gbc, 2, 0.2);

        nextRow(gbc);
        addLabel(details, getText("bindings.profileName"), gbc);
        profileField = readOnlyField();
        addField(details, profileField, gbc, 1, 1.0);

        nextRow(gbc);
        addLabel(details, getText("bindings.filePath"), gbc);
        filePathField = readOnlyField();
        addField(details, filePathField, gbc, 1, 1.0);

        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel keyboardOnlyHint = new JLabel(getText("bindings.keyboardOnlyHint"));
        keyboardOnlyHint.setForeground(FG_MUTED);
        details.add(keyboardOnlyHint, gbc);

        add(details, BorderLayout.NORTH);

        usedBindingsPanel = groupedTablesPanel();
        missingBindingsPanel = groupedTablesPanel();
        usedBindingsScrollPane = groupedTablesScrollPane(usedBindingsPanel);
        missingBindingsScrollPane = groupedTablesScrollPane(missingBindingsPanel);

        tabs = new JTabbedPane();
        AppTheme.styleTabbedPane(tabs);
        tabs.setFont(tabs.getFont().deriveFont(Font.BOLD, tabs.getFont().getSize2D() + 1f));
        tabs.addTab(getText("bindings.usedBindings"), nestedTabContent(usedBindingsScrollPane));
        tabs.addTab(getText("bindings.missingBindings"), nestedTabContent(missingBindingsScrollPane));
        add(tabs, BorderLayout.CENTER);
    }

    public void initData() {
        clearOuterScrollPaneBorders();
        bindingsDirField.setText(playerSession.getBindingsDir().toString());
        try {
            File bindingsFile = activeBindingsFile();
            Map<String, KeyBindingsParser.ReadOnlyBindingSlots> slots = parser.parseReadOnlyBindingSlots(bindingsFile);
            Map<String, KeyBindingsParser.KeyBinding> parsedBindings = effectiveBindings(slots);

            profileField.setText(activeProfileName(bindingsFile));
            filePathField.setText(bindingsFile.getAbsolutePath());

            List<String> usedBindings = monitor.findFoundGameBindings(parsedBindings).stream()
                    .sorted(String::compareToIgnoreCase)
                    .toList();
            renderGroupedTables(
                    usedBindingsPanel,
                    groupedUsedBindings(usedBindings, slots),
                    getText("bindings.column.action"),
                    getText("bindings.column.primary"),
                    getText("bindings.column.secondary"));
            tabs.setTitleAt(0, getText("bindings.usedBindings", usedBindings.size()));

            List<String> missingBindings = monitor.findMissingGameBindings(parsedBindings).stream()
                    .sorted(String::compareToIgnoreCase)
                    .toList();
            renderGroupedTables(
                    missingBindingsPanel,
                    groupedMissingBindings(missingBindings, slots),
                    getText("bindings.column.action"),
                    getText("bindings.column.primary"),
                    getText("bindings.column.secondary"));
            tabs.setTitleAt(1, getText("bindings.missingBindings", missingBindings.size()));
        } catch (Exception e) {
            profileField.setText(getText("bindings.notAvailable"));
            filePathField.setText(getText("bindings.notAvailable"));
            renderGroupedTables(usedBindingsPanel, Map.of(), getText("bindings.column.action"));
            renderGroupedTables(missingBindingsPanel, Map.of(), getText("bindings.column.action"));
            tabs.setTitleAt(0, getText("bindings.usedBindings", 0));
            tabs.setTitleAt(1, getText("bindings.missingBindings", 0));
        }
    }

    private File activeBindingsFile() throws Exception {
        File currentFile = monitor.getCurrentBindsFile();
        return currentFile != null ? currentFile : loader.getLatestBindsFile();
    }

    private String activeProfileName(File bindingsFile) {
        String presetName = loader.getActivePresetName();
        if (presetName != null && !presetName.isBlank())
            return presetName;

        String fileName = bindingsFile.getName();
        int profileEnd = fileName.indexOf('.');
        return profileEnd > 0 ? fileName.substring(0, profileEnd) : fileName;
    }

    private JTextField readOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setPreferredSize(new Dimension(0, 42));
        field.setBackground(BG_PANEL);
        field.setForeground(FG);
        return field;
    }

    private JPanel groupedTablesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        return panel;
    }

    private JScrollPane groupedTablesScrollPane(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(TABLE_ROW_HEIGHT * SCROLL_UNIT_ROWS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private void clearOuterScrollPaneBorders() {
        // AppTheme styles all scroll panes after buildUi; keep the Bindings content
        // panes visually borderless.
        if (usedBindingsScrollPane != null)
            usedBindingsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        if (missingBindingsScrollPane != null)
            missingBindingsScrollPane.setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel nestedTabContent(JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(6, 0, 0, 0));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private Map<BindingGroup, List<Object[]>> groupedUsedBindings(List<String> bindingIds,
            Map<String, KeyBindingsParser.ReadOnlyBindingSlots> slots) {
        Map<BindingGroup, List<Object[]>> grouped = groupedRows();
        for (String bindingId : bindingIds) {
            KeyBindingsParser.ReadOnlyBindingSlots bindingSlots = slots.get(bindingId);
            grouped.get(BindingGroupClassifier.classify(bindingId)).add(new Object[] {
                    bindingId,
                    formatSlot(bindingSlots == null ? null : bindingSlots.primary()),
                    formatSlot(bindingSlots == null ? null : bindingSlots.secondary())
            });
        }
        return grouped;
    }

    /**
     * Builds the same keyboard-only view that command execution uses while keeping
     * diagnostic slots available for tables.
     * <p>
     * Non-keyboard slots remain visible in the read-only UI, but they are not
     * included in this map and therefore still
     * count as missing for EliteIntel command execution.
     */
    private Map<String, KeyBindingsParser.KeyBinding> effectiveBindings(
            Map<String, KeyBindingsParser.ReadOnlyBindingSlots> slots) {
        Map<String, KeyBindingsParser.KeyBinding> bindings = new HashMap<>();
        for (Map.Entry<String, KeyBindingsParser.ReadOnlyBindingSlots> entry : slots.entrySet()) {
            KeyBindingsParser.KeyBinding keyBinding = executableBinding(entry.getValue().primary());
            if (keyBinding == null)
                keyBinding = executableBinding(entry.getValue().secondary());
            if (keyBinding != null)
                bindings.put(entry.getKey(), keyBinding);
        }
        return bindings;
    }

    private KeyBindingsParser.KeyBinding executableBinding(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        if (slot == null || !slot.keyboardUsable())
            return null;
        return parser.new KeyBinding(slot.key(), slot.modifiers(), slot.hold());
    }

    private Map<BindingGroup, List<Object[]>> groupedMissingBindings(List<String> bindingIds,
            Map<String, KeyBindingsParser.ReadOnlyBindingSlots> slots) {
        Map<BindingGroup, List<Object[]>> grouped = groupedRows();
        for (String bindingId : bindingIds) {
            KeyBindingsParser.ReadOnlyBindingSlots bindingSlots = slots.get(bindingId);
            grouped.get(BindingGroupClassifier.classify(bindingId)).add(new Object[] {
                    bindingId,
                    formatSlot(bindingSlots == null ? null : bindingSlots.primary()),
                    formatSlot(bindingSlots == null ? null : bindingSlots.secondary())
            });
        }
        return grouped;
    }

    private Map<BindingGroup, List<Object[]>> groupedRows() {
        Map<BindingGroup, List<Object[]>> grouped = new EnumMap<>(BindingGroup.class);
        for (BindingGroup group : BindingGroup.values()) {
            grouped.put(group, new ArrayList<>());
        }
        return grouped;
    }

    private void renderGroupedTables(JPanel targetPanel, Map<BindingGroup, List<Object[]>> grouped,
            String... columnNames) {
        targetPanel.removeAll();
        for (BindingGroup group : BindingGroup.values()) {
            List<Object[]> rows = grouped.getOrDefault(group, List.of()).stream()
                    .sorted(Comparator.comparing(row -> row[0].toString(), String.CASE_INSENSITIVE_ORDER))
                    .toList();
            if (rows.isEmpty())
                continue;

            targetPanel.add(sectionHeader(group));
            targetPanel.add(groupTable(rows, outerScrollPaneFor(targetPanel), columnNames));
            targetPanel.add(Box.createVerticalStrut(12));
        }
        targetPanel.add(Box.createVerticalGlue());
        targetPanel.revalidate();
        targetPanel.repaint();
    }

    private JLabel sectionHeader(BindingGroup group) {
        JLabel label = new JLabel(getText(group.getLabelKey()));
        label.setForeground(ACCENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D() + 2f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(14, 0, 6, 0));
        return label;
    }

    private JScrollPane outerScrollPaneFor(JPanel targetPanel) {
        return targetPanel == usedBindingsPanel ? usedBindingsScrollPane : missingBindingsScrollPane;
    }

    private JScrollPane groupTable(List<Object[]> rows, JScrollPane outerScrollPane, String... columnNames) {
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] row : rows) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        styleGroupTable(table);
        configureColumnWidths(table);
        table.setPreferredScrollableViewportSize(new Dimension(0, table.getRowHeight() * Math.max(1, rows.size())));
        forwardMouseWheelToOuterScrollPane(table, outerScrollPane);
        forwardMouseWheelToOuterScrollPane(table.getTableHeader(), outerScrollPane);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.getViewport().setBackground(BG_PANEL);
        scrollPane.setBorder(TABLE_SECTION_BORDER);
        forwardMouseWheelToOuterScrollPane(scrollPane, outerScrollPane);
        forwardMouseWheelToOuterScrollPane(scrollPane.getViewport(), outerScrollPane);

        int height = table.getRowHeight() * rows.size() + table.getTableHeader().getPreferredSize().height + 6;
        scrollPane.setPreferredSize(new Dimension(0, height));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return scrollPane;
    }

    private void styleGroupTable(JTable table) {
        table.setFillsViewportHeight(false);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.setAutoCreateRowSorter(false);
        table.setBackground(BG_PANEL);
        table.setForeground(FG);
        table.setShowGrid(false);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(BG);
        table.getTableHeader().setForeground(FG);
        table.getTableHeader().setBorder(TABLE_HEADER_BORDER);
        table.getTableHeader().setDefaultRenderer(new GroupTableHeaderRenderer());
        table.setDefaultRenderer(Object.class, new GroupTableCellRenderer());
    }

    private void configureColumnWidths(JTable table) {
        if (table.getColumnCount() < 3)
            return;

        table.getColumnModel().getColumn(0).setPreferredWidth(320);
        table.getColumnModel().getColumn(1).setPreferredWidth(270);
        table.getColumnModel().getColumn(2).setPreferredWidth(270);
        if (table.getColumnCount() > 3) {
            table.getColumnModel().getColumn(3).setPreferredWidth(180);
        }
    }

    private static class GroupTableCellRenderer extends DefaultTableCellRenderer {
        private GroupTableCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            label.setBackground(BG_PANEL);
            boolean notDefined = elite.intel.ui.i18n.MultiLingualTextProvider
                    .getText("bindings.status.notDefined")
                    .equals(value);
            label.setForeground(notDefined ? FG_MUTED : FG);
            return label;
        }
    }

    private static class GroupTableHeaderRenderer extends DefaultTableCellRenderer {
        private GroupTableHeaderRenderer() {
            setOpaque(true);
            setBorder(TABLE_HEADER_BORDER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            label.setBackground(BG);
            label.setForeground(FG);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setBorder(TABLE_HEADER_BORDER);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            return label;
        }
    }

    private void forwardMouseWheelToOuterScrollPane(JComponent source, JScrollPane outerScrollPane) {
        source.addMouseWheelListener(event -> {
            Point point = SwingUtilities.convertPoint(source, event.getPoint(), outerScrollPane);
            MouseWheelEvent converted = new MouseWheelEvent(
                    outerScrollPane,
                    event.getID(),
                    event.getWhen(),
                    event.getModifiersEx(),
                    point.x,
                    point.y,
                    event.getXOnScreen(),
                    event.getYOnScreen(),
                    event.getClickCount(),
                    event.isPopupTrigger(),
                    event.getScrollType(),
                    event.getScrollAmount(),
                    event.getWheelRotation(),
                    event.getPreciseWheelRotation());
            outerScrollPane.dispatchEvent(converted);
            event.consume();
        });
    }

    private String formatSlot(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        if (isEmptySlot(slot)) {
            return getText("bindings.status.notDefined");
        }
        return formatDevice(slot) + " | " + formatBinding(slot);
    }

    private boolean isEmptySlot(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        return isEmptyDevice(slot)
                && (slot == null || slot.key() == null || slot.key().isBlank() || "Key_".equals(slot.key()));
    }

    private String formatDevice(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        if (isEmptyDevice(slot)) {
            return "—";
        }
        return isRawDeviceId(slot.device()) ? "Device " + slot.device() : slot.device();
    }

    private boolean isEmptyDevice(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        return slot == null || slot.device() == null || slot.device().isBlank() || "{NoDevice}".equals(slot.device());
    }

    private boolean isRawDeviceId(String device) {
        return device.matches("(?i)[0-9a-f]{8}");
    }

    private String formatBinding(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        if (slot == null || slot.key() == null || slot.key().isBlank() || "Key_".equals(slot.key())) {
            return getText("bindings.status.notDefined");
        }

        String[] modifiers = slot.modifiers() == null
                ? new String[0]
                : Arrays.stream(slot.modifiers())
                        .filter(modifier -> modifier != null && !modifier.isBlank() && !"Key_".equals(modifier))
                        .map(this::formatBindingToken)
                        .sorted(Comparator.naturalOrder())
                        .toArray(String[]::new);
        String key = formatBindingToken(slot.key());
        if (modifiers.length == 0)
            return key;

        return String.join(" + ", modifiers) + " + " + key;
    }

    private String formatBindingToken(String token) {
        if (token.startsWith("Key_") && token.length() > "Key_".length()) {
            return "Key '" + token.substring("Key_".length()) + "'";
        }
        if (token.startsWith("Joy_") && token.length() > "Joy_".length()) {
            return "Joystick " + token.substring("Joy_".length());
        }
        return token;
    }

}

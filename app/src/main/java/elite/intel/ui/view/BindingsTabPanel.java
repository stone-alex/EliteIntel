package elite.intel.ui.view;

import elite.intel.ai.hands.BindingGroup;
import elite.intel.ai.hands.BindingGroupClassifier;
import elite.intel.ai.hands.BindingModifier;
import elite.intel.ai.hands.BindingsApplyException;
import elite.intel.ai.hands.BindingsApplyService;
import elite.intel.ai.hands.BindingSaveResult;
import elite.intel.ai.hands.BindingSlotType;
import elite.intel.ai.hands.BindingsLoader;
import elite.intel.ai.hands.BindingsMonitor;
import elite.intel.ai.hands.BindingsWorkingCopyRepository;
import elite.intel.ai.hands.BindingsWriter;
import elite.intel.ai.hands.KeyBindingsParser;
import elite.intel.ai.hands.KeyboardBindingEdit;
import elite.intel.ai.hands.KeyboardKeyAvailabilityService;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.BindingsUpdatedEvent;
import com.google.common.eventbus.Subscribe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;
import static elite.intel.ui.view.AppTheme.*;

public class BindingsTabPanel extends JPanel {

    private static final int SCROLL_UNIT_ROWS = 2;

    private final BindingsLoader loader = new BindingsLoader();
    private final KeyBindingsParser parser = KeyBindingsParser.getInstance();
    private final BindingsMonitor monitor = BindingsMonitor.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final KeyboardKeyAvailabilityService availabilityService = new KeyboardKeyAvailabilityService();
    private final BindingsWriter bindingsWriter = new BindingsWriter();
    private final BindingSlotDisplayFormatter slotFormatter = new BindingSlotDisplayFormatter();
    private final BindingsSelectionController selectionController;
    private final BindingsGroupTableFactory tableFactory;
    private final BindingsWorkingCopyRepository workingCopyRepo = new BindingsWorkingCopyRepository();
    private final BindingsApplyService applyService = new BindingsApplyService();
    private final List<JButton> headerInfoButtons = new ArrayList<>();

    private JTextField profileField;
    private JTextField filePathField;
    private JTextField bindingsDirField;
    private JPanel keyboardOnlyBanner;
    private JLabel keyboardOnlyBannerText;
    private BindingSaveResultPresenter saveResultPresenter;
    private JPanel usedBindingsPanel;
    private JPanel missingBindingsPanel;
    private JScrollPane usedBindingsScrollPane;
    private JScrollPane missingBindingsScrollPane;
    private JTabbedPane tabs;
    private static final Color STATUS_SYNCED_COLOR = new Color(0x4CAF50);

    private JLabel syncStatusIcon;
    private JLabel syncStatusLabel;
    private JButton applyButton;
    private JButton revertButton;

    private Map<String, KeyBindingsParser.ReadOnlyBindingSlots> currentSlots = Map.of();
    /** Working copy file currently loaded in the editor — used for stale checks. */
    private File activeBindingsFile;
    private FileTime activeBindingsLastModified;
    private long activeBindingsFileSize = -1;
    /** The actual game binds file — source for Apply and for the file-path display field. */
    private File gameBindingsFile;
    /** The preset file name (e.g. {@code Custom.3.0.binds}) — key for the working copy. */
    private String activePresetFileName;
    private boolean assignDialogOpen;

    public BindingsTabPanel() {
        selectionController = new BindingsSelectionController();
        tableFactory = new BindingsGroupTableFactory(selectionController, this::openAssignKeyboardBindingDialog);
        buildUi();
        saveResultPresenter = new BindingSaveResultPresenter(this);
        EventBusManager.register(this);
    }

    public void dispose() {
        EventBusManager.unregister(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::applyHeaderDisplayStyle);
    }

    @Subscribe
    public void onBindingsUpdated(BindingsUpdatedEvent event) {
        SwingUtilities.invokeLater(this::initData);
    }

    /**
     * Returns {@code true} if a draft working copy exists that differs from the
     * current game file. Used by the parent window to decide whether to show a
     * close confirmation dialog.
     */
    public boolean hasUnappliedChanges() {
        if (activePresetFileName == null || gameBindingsFile == null) {
            return false;
        }
        return !workingCopyRepo.isSyncedWithGame(activePresetFileName, gameBindingsFile.toPath());
    }

    /**
     * Shows a modal dialog when the user closes the application with an unapplied
     * draft. Offers Apply, Keep Draft, or Discard. Blocks until the user responds.
     * Must be called on the EDT.
     */
    public void promptCloseWithDraft() {
        if (!hasUnappliedChanges()) {
            return;
        }
        Object[] options = {
                getText("bindings.close.draft.apply"),
                getText("bindings.close.draft.keep"),
                getText("bindings.close.draft.discard")
        };
        int choice = JOptionPane.showOptionDialog(
                this,
                getText("bindings.close.draft.text"),
                getText("bindings.close.draft.title"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1]
        );
        if (choice == 0) {
            // Apply
            performApply();
        } else if (choice == 2) {
            // Discard
            if (activePresetFileName != null) {
                workingCopyRepo.delete(activePresetFileName);
            }
        }
        // choice == 1 (Keep Draft) or dialog closed: do nothing, draft is already on disk
    }

    private void buildUi() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BG);

        JPanel details = new JPanel(new GridBagLayout());
        details.setOpaque(false);
        GridBagConstraints gbc = baseGbc();
        // Compact header: ~30% less vertical space than the app-wide default (42px / 12px insets → 30px / 4px).
        gbc.insets = new Insets(2, 6, 2, 6);

        resetHeaderRow(gbc);
        addHeaderLabel(details, getText("player.bindingsDirectory"), gbc);
        bindingsDirField = readOnlyField();
        bindingsDirField.setToolTipText(getText("player.bindingsDirectory.tooltip"));
        addField(details, bindingsDirField, gbc, 1, 1.0);
        JButton selectBindingsDirButton = compactDirectoryChooserButton();
        selectBindingsDirButton.addActionListener(e -> selectBindingsDirectory());
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        details.add(selectBindingsDirButton, gbc);

        nextRow(gbc);
        resetHeaderRow(gbc);
        addHeaderLabel(details, getText("bindings.profileName"), gbc);
        profileField = readOnlyValueField();
        addField(details, profileField, gbc, 1, 1.0);
        addInfoButton(details, gbc, "bindings.profileName.info");

        nextRow(gbc);
        resetHeaderRow(gbc);
        addHeaderLabel(details, getText("bindings.filePath"), gbc);
        filePathField = readOnlyValueField();
        addField(details, filePathField, gbc, 1, 1.0);
        addInfoButton(details, gbc, "bindings.filePath.info");

        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        details.add(keyboardOnlyBanner(), gbc);

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
        tabs.addChangeListener(e -> selectionController.clearSelection());

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBackground(BG);
        center.add(tabs, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildFooter() {
        syncStatusIcon = new JLabel("●");   // ● BLACK CIRCLE
        syncStatusIcon.setFont(syncStatusIcon.getFont().deriveFont(Font.PLAIN, 10f));

        syncStatusLabel = new JLabel();
        syncStatusLabel.setFont(syncStatusLabel.getFont().deriveFont(Font.PLAIN));

        JPanel statusArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        statusArea.setOpaque(false);
        statusArea.add(syncStatusIcon);
        statusArea.add(syncStatusLabel);

        revertButton = makeButtonSubtle(getText("bindings.button.revert"));
        revertButton.addActionListener(e -> revertFromGame());

        applyButton = makeButton(getText("bindings.button.apply"));
        applyButton.addActionListener(e -> performApply());

        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x35354A)),
                BorderFactory.createEmptyBorder(6, 0, 0, 0)));

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setOpaque(false);
        buttonBar.add(revertButton);
        buttonBar.add(applyButton);

        footer.add(statusArea, BorderLayout.CENTER);
        footer.add(buttonBar, BorderLayout.EAST);
        return footer;
    }

    public void initData() {
        clearOuterScrollPaneBorders();
        selectionController.resetTables();
        bindingsDirField.setText(playerSession.getBindingsDir().toString());
        try {
            File resolvedGameFile = resolveGameBindsFile();
            String presetFileName = resolvedGameFile.getName();

            Path workingCopyPath = workingCopyRepo.loadOrImportFromGame(
                    presetFileName, resolvedGameFile.toPath());

            Map<String, KeyBindingsParser.ReadOnlyBindingSlots> slots =
                    parser.parseReadOnlyBindingSlots(workingCopyPath.toFile());
            Map<String, KeyBindingsParser.KeyBinding> parsedBindings = effectiveBindings(slots);

            currentSlots = slots;
            activeBindingsFile = workingCopyPath.toFile();
            activeBindingsLastModified = Files.getLastModifiedTime(workingCopyPath);
            activeBindingsFileSize = Files.size(workingCopyPath);
            gameBindingsFile = resolvedGameFile;
            activePresetFileName = presetFileName;

            profileField.setText(activeProfileName(resolvedGameFile));
            filePathField.setText(resolvedGameFile.getAbsolutePath());

            List<String> usedBindings = monitor.findFoundGameBindings(parsedBindings).stream()
                    .sorted(String::compareToIgnoreCase)
                    .toList();
            renderGroupedTables(
                    usedBindingsPanel,
                    groupedBindings(usedBindings, slots),
                    getText("bindings.column.action"),
                    getText("bindings.column.primary"),
                    getText("bindings.column.secondary"));
            tabs.setTitleAt(0, getText("bindings.usedBindings", usedBindings.size()));

            List<String> missingBindings = monitor.findMissingGameBindings(parsedBindings).stream()
                    .sorted(String::compareToIgnoreCase)
                    .toList();
            renderGroupedTables(
                    missingBindingsPanel,
                    groupedBindings(missingBindings, slots),
                    getText("bindings.column.action"),
                    getText("bindings.column.primary"),
                    getText("bindings.column.secondary"));
            tabs.setTitleAt(1, getText("bindings.missingBindings", missingBindings.size()));
        } catch (Exception e) {
            clearLoadedBindingsSnapshot();
            profileField.setText(getText("bindings.notAvailable"));
            filePathField.setText(getText("bindings.notAvailable"));
            renderGroupedTables(usedBindingsPanel, Map.of(), getText("bindings.column.action"));
            renderGroupedTables(missingBindingsPanel, Map.of(), getText("bindings.column.action"));
            tabs.setTitleAt(0, getText("bindings.usedBindings", 0));
            tabs.setTitleAt(1, getText("bindings.missingBindings", 0));
        }
        updateSyncStatus();
    }

    private void updateSyncStatus() {
        if (applyButton == null || syncStatusLabel == null || syncStatusIcon == null) {
            return;
        }
        boolean synced = activePresetFileName == null
                || gameBindingsFile == null
                || workingCopyRepo.isSyncedWithGame(activePresetFileName, gameBindingsFile.toPath());

        Color statusColor = synced ? STATUS_SYNCED_COLOR : ACCENT;
        syncStatusIcon.setForeground(statusColor);
        syncStatusLabel.setText(synced
                ? getText("bindings.status.synced")
                : getText("bindings.status.draft"));
        syncStatusLabel.setForeground(statusColor);

        applyButton.setEnabled(!synced && activePresetFileName != null);
        revertButton.setEnabled(activePresetFileName != null && workingCopyRepo.exists(activePresetFileName));
    }

    private void performApply() {
        if (activePresetFileName == null || gameBindingsFile == null) {
            return;
        }
        try {
            Path backupPath = applyService.apply(activePresetFileName, gameBindingsFile.toPath());
            String successMsg = backupPath != null
                    ? getText("bindings.apply.success", backupPath.getFileName())
                    : getText("bindings.apply.success.noBackup");
            JOptionPane.showMessageDialog(
                    this,
                    successMsg,
                    getText("bindings.apply.dialogTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            updateSyncStatus();
        } catch (BindingsApplyException e) {
            JOptionPane.showMessageDialog(
                    this,
                    getText("bindings.apply.error", e.getMessage()),
                    getText("bindings.apply.dialogTitle"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void revertFromGame() {
        if (activePresetFileName == null || gameBindingsFile == null) {
            return;
        }
        int response = JOptionPane.showConfirmDialog(
                this,
                getText("bindings.revert.confirm.text"),
                getText("bindings.revert.confirm.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            workingCopyRepo.delete(activePresetFileName);
            initData();
        }
    }

    private void selectBindingsDirectory() {
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
    }

    private File resolveGameBindsFile() throws Exception {
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

    private void addHeaderLabel(JPanel panel, String text, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(220, HEADER_ROW_HEIGHT));
        panel.add(label, gbc);
    }

    private void resetHeaderRow(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
    }

    private static final int HEADER_ROW_HEIGHT = 30;

    private JTextField readOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setPreferredSize(new Dimension(0, HEADER_ROW_HEIGHT));
        field.setBackground(BG_PANEL);
        field.setForeground(FG);
        return field;
    }

    private JTextField readOnlyValueField() {
        JTextField field = readOnlyField();
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        field.setForeground(FG_MUTED);
        field.setCaretColor(FG_MUTED);
        return field;
    }

    private JButton compactDirectoryChooserButton() {
        JButton button = makeButton("⋮");
        button.setToolTipText(getText("player.bindingsDirectory.select.tooltip"));
        Dimension size = new Dimension(HEADER_ROW_HEIGHT, HEADER_ROW_HEIGHT);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        return button;
    }

    private void addInfoButton(JPanel panel, GridBagConstraints gbc, String messageKey) {
        JButton button = new JButton("ⓘ");
        String message = getText(messageKey);
        button.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                message,
                getText("bindings.info.title"),
                JOptionPane.INFORMATION_MESSAGE));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setForeground(FG_MUTED);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, button.getFont().getSize2D() + 2f));
        Dimension size = new Dimension(HEADER_ROW_HEIGHT, HEADER_ROW_HEIGHT);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        headerInfoButtons.add(button);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(button, gbc);
    }

    private void applyHeaderDisplayStyle() {
        styleReadOnlyValueField(profileField);
        styleReadOnlyValueField(filePathField);
        headerInfoButtons.forEach(this::styleHeaderInfoButton);
        styleKeyboardOnlyBanner();
    }

    private void styleReadOnlyValueField(JTextField field) {
        if (field == null) {
            return;
        }
        field.setOpaque(false);
        field.setBackground(BG);
        field.setForeground(FG_MUTED);
        field.setCaretColor(FG_MUTED);
        field.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
    }

    private void styleHeaderInfoButton(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setForeground(FG_MUTED);
        button.setBackground(BG);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, button.getFont().getSize2D() + 2f));
        Dimension size = new Dimension(HEADER_ROW_HEIGHT, HEADER_ROW_HEIGHT);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
    }

    private JPanel keyboardOnlyBanner() {
        keyboardOnlyBanner = new JPanel(new BorderLayout());
        keyboardOnlyBannerText = new JLabel("⚠  " + getText("bindings.keyboardOnlyHint"));
        keyboardOnlyBanner.add(keyboardOnlyBannerText, BorderLayout.CENTER);
        styleKeyboardOnlyBanner();
        return keyboardOnlyBanner;
    }

    private void styleKeyboardOnlyBanner() {
        if (keyboardOnlyBanner == null) {
            return;
        }
        keyboardOnlyBanner.setOpaque(true);
        keyboardOnlyBanner.setBackground(new Color(0x2A2418));
        keyboardOnlyBanner.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        keyboardOnlyBannerText.setForeground(ACCENT);
        keyboardOnlyBannerText.setHorizontalAlignment(SwingConstants.CENTER);
        keyboardOnlyBannerText.setFont(keyboardOnlyBannerText.getFont().deriveFont(Font.BOLD));
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
        scrollPane.getVerticalScrollBar().setUnitIncrement(BindingsGroupTableFactory.TABLE_ROW_HEIGHT * SCROLL_UNIT_ROWS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private void clearOuterScrollPaneBorders() {
        // AppTheme styles all scroll panes after buildUi; keep the Bindings content panes visually borderless.
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

    private Map<BindingGroup, List<Object[]>> groupedBindings(
            List<String> bindingIds,
            Map<String, KeyBindingsParser.ReadOnlyBindingSlots> slots
    ) {
        Map<BindingGroup, List<Object[]>> grouped = groupedRows();
        for (String bindingId : bindingIds) {
            KeyBindingsParser.ReadOnlyBindingSlots bindingSlots = slots.get(bindingId);
            grouped.get(BindingGroupClassifier.classify(bindingId)).add(new Object[] {
                    bindingId,
                    slotFormatter.formatSlot(bindingSlots == null ? null : bindingSlots.primary()),
                    slotFormatter.formatSlot(bindingSlots == null ? null : bindingSlots.secondary())
            });
        }
        return grouped;
    }

    /**
     * Builds the same keyboard-only view that command execution uses while keeping
     * diagnostic slots available for tables.
     * <p>
     * Non-keyboard slots remain visible in the read-only UI, but they are not included
     * in this map and therefore still count as missing for EliteIntel command execution.
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
            targetPanel.add(tableFactory.groupTable(rows, outerScrollPaneFor(targetPanel), columnNames));
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

    private void openAssignKeyboardBindingDialog(String bindingId, BindingSlotType slotType) {
        if (bindingId == null || bindingId.isBlank() || activeBindingsFile == null || assignDialogOpen) {
            return;
        }

        KeyBindingsParser.ReadOnlyBindingSlots slots = currentSlots.get(bindingId);
        KeyBindingsParser.ReadOnlyBindingSlot slot = slotType == BindingSlotType.PRIMARY
                ? (slots == null ? null : slots.primary())
                : (slots == null ? null : slots.secondary());
        if (!isBasicEditableSlot(slot)) {
            JOptionPane.showMessageDialog(
                    this,
                    getText("bindings.assign.unsupportedReadOnlyMessage"),
                    getText("bindings.assign.dialogTitle"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        AssignKeyboardBindingDialog dialog = new AssignKeyboardBindingDialog(
                this,
                activeBindingsFile.toPath(),
                bindingId,
                slotType,
                slot,
                availabilityService
        );

        assignDialogOpen = true;
        try {
            dialog.showDialog().ifPresent(selection -> saveKeyboardBinding(bindingId, selection));
        } finally {
            assignDialogOpen = false;
        }
    }

    private void saveKeyboardBinding(String bindingId, AssignKeyboardBindingSelection selection) {
        if (activeBindingsFile == null || activeBindingsLastModified == null || activeBindingsFileSize < 0) {
            saveResultPresenter.showWriteFailed();
            return;
        }

        Path file = activeBindingsFile.toPath();
        KeyboardBindingEdit edit = new KeyboardBindingEdit(
                file,
                bindingId,
                selection.slotType(),
                selection.key(),
                activeBindingsLastModified,
                activeBindingsFileSize
        );
        BindingSaveResult result = saveKeyboardBinding(edit, selection.modifier());
        saveResultPresenter.show(result);

        if (result == BindingSaveResult.SAVED || result == BindingSaveResult.NO_CHANGE || result == BindingSaveResult.STALE_FILE) {
            initData();
        }
    }

    private BindingSaveResult saveKeyboardBinding(KeyboardBindingEdit edit, BindingModifier modifier) {
        return modifier == null
                ? bindingsWriter.assignKeyboardKey(edit)
                : bindingsWriter.assignKeyboardKeyWithModifier(edit, modifier);
    }

    private boolean isBasicEditableSlot(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        return slot == null || slot.editable() || isClearedSlot(slot);
    }

    private boolean isClearedSlot(KeyBindingsParser.ReadOnlyBindingSlot slot) {
        return "{NoDevice}".equals(slot.device())
                && (slot.key() == null || slot.key().isBlank());
    }

    private void clearLoadedBindingsSnapshot() {
        currentSlots = Map.of();
        activeBindingsFile = null;
        activeBindingsLastModified = null;
        activeBindingsFileSize = -1;
        gameBindingsFile = null;
        activePresetFileName = null;
    }
}

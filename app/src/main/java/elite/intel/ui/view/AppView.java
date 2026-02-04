package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.*;
import elite.intel.util.SleepNoThrow;
import elite.intel.util.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the main graphical user interface of the application.
 * The `AppView` class organizes and manages user interactions, layouts,
 * and theming for various sections such as system configuration, player
 * configuration, and help documentation.
 */
public class AppView extends JFrame implements AppViewInterface {

    public static final String LABEL_STREAMING_MODE = "Streaming Mode";
    public static final String LABEL_PRIVACY_MODE = "Voice Input on/off";
    // ----- COLORS (adjust to taste) -----
    private static final Color BG = new Color(0x141622); // base background
    private static final Color LOG_BG = new Color(0x141622); // base background
    private static final Color BG_PANEL = new Color(0x1F2032); // panels/inputs background
    private static final Color FG = new Color(0xE6E6E6); // primary text
    private static final Color FG_MUTED = new Color(0xB0B0B0); // secondary text
    private static final Color ACCENT = new Color(0xFF8C00); // orange
    private static final Color CONSOLE_FG = new Color(0xE0FFEF); // orange
    private static final Color SEL_BG = new Color(0xE0FFEF); // selection background
    private static final Color SEL_FG = new Color(0x13181D); // selection background
    private static final Color TAB_UNSELECTED = new Color(0x141622);
    private static final Color TAB_SELECTED = new Color(0x1F2032);
    private static final Color DISABLED_FG = new Color(0x8B0101);
    private static final String ICON_AI = "/images/ai.png";
    private static final String ICON_PLAYER = "/images/controller.png";
    private static final String ICON_SETTINGS = "/images/settings.png";
    private static final Logger log = LoggerFactory.getLogger(AppView.class);
    private final PlayerSession playerSession = PlayerSession.getInstance();
    private final SystemSession systemSession = SystemSession.getInstance();
    // Title
    private final JLabel titleLabel;
    private final AtomicBoolean isServiceRunning = new AtomicBoolean(false);
    public JCheckBox toggleStreamingModeCheckBox;
    public JTextArea logArea;
    private Font monoFont;
    // System tab components
    private JPasswordField sttApiKeyField;
    private JCheckBox sttLockedCheck;
    private JPasswordField llmApiKeyField;
    private JTextField localLlmAddressField;
    private JTextField localLlmModelCommandField;
    private JTextField localLlmModelQueryField;
    private JCheckBox llmLockedCheck;
    private JCheckBox showDetailedLog;
    private JPasswordField ttsApiKeyField;
    private JCheckBox ttsLockedCheck;
    private JCheckBox useLocalCommandLLMCheck;
    private JCheckBox useLocalQueryLLMCheck;
    private JCheckBox useLocalTTSCheck;
    private JToggleButton startStopServicesButton;
    private JButton recalibrateAudioButton;
    private JButton updateAppButton;
    private JCheckBox togglePrivacyModeCheckBox;
    private JPasswordField edsmKeyField;
    private JCheckBox edsmLockedCheck;
    // Player tab components
    private JCheckBox sendMarketData;
    private JCheckBox sendShipyardData;
    private JCheckBox sendOutfitingData;
    private JCheckBox sendExplorationData;

    private JTextField playerAltNameField;
    private JTextField playerTitleField;
    private JTextField playerMissionDescription;
    private JTextField journalDirField;
    private JTextField localTtsAddressField;
    private JSlider speechSpeedSlider;
    private JLabel speechSpeedLabel;

    // ---------- Public API ----------
    private JTextField bindingsDirField;

    public AppView() {
        super("--");
        setTitle("Elite Intel " + systemSession.readVersionFromResources());
        // Load and apply custom font before any other UI setup
        loadCustomFont();
        // Apply dark theme defaults
        installDarkDefaults();
        EventBusManager.register(this);

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/elite-logo.png")));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 800));
        setSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        titleLabel = new JLabel("Elite Intel", SwingConstants.CENTER);
        // Make the title a bit larger than the base
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, titleLabel.getFont().getSize2D() + 3f));
        root.add(titleLabel, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        styleTabbedPane(tabs);


        ImageIcon aiIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(ICON_AI))).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        ImageIcon playerIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(ICON_PLAYER))).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        ImageIcon settingsIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(ICON_SETTINGS))).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));

        tabs.addTab("Ai", aiIcon, buildAiTab());
        tabs.addTab("Player", playerIcon, buildPlayerTab());
        tabs.addTab("Settings", settingsIcon, buildSettingsTab());

        root.add(tabs, BorderLayout.CENTER);
        applyDarkPalette(getContentPane());

        //initial state
        bindLock(sttLockedCheck, sttApiKeyField);
        bindLock(llmLockedCheck, llmApiKeyField);
        bindLock(ttsLockedCheck, ttsApiKeyField);
        bindLock(edsmLockedCheck, edsmKeyField);

        toggleStreamingModeCheckBox.setEnabled(false);//enabled when services start
        toggleStreamingModeCheckBox.setToolTipText("Prevent AI from processing unless you prefix your command or query with word 'computer'");
        toggleStreamingModeCheckBox.setText(LABEL_STREAMING_MODE);
        toggleStreamingModeCheckBox.setForeground(ACCENT);
        showDetailedLog.setForeground(ACCENT);

        togglePrivacyModeCheckBox.setEnabled(false); // enabled when services start
        togglePrivacyModeCheckBox.setToolTipText("Disable Speech to Text completely");
        togglePrivacyModeCheckBox.setText(LABEL_PRIVACY_MODE);
        togglePrivacyModeCheckBox.setForeground(ACCENT);

        journalDirField.setEditable(false);
        journalDirField.setPreferredSize(new Dimension(200, 42));
        bindingsDirField.setEditable(false);
        bindingsDirField.setPreferredSize(new Dimension(200, 42));
        recalibrateAudioButton.setEnabled(false);

        initData();
    }

    /**
     * Binds a lock checkbox to a specific field, allowing the field's state
     * (enabled, editable, or read-only) to be toggled based on the checkbox selection.
     *
     * @param lockCheck The checkbox that controls the locking mechanism.
     * @param field     The UI component whose state will be controlled by the checkbox.
     *                  Supports JTextComponent and other JComponent types.
     */
    private static void bindLock(JCheckBox lockCheck, JComponent field) {
        Runnable apply = () -> {
            boolean locked = lockCheck.isSelected();
            if (field instanceof JTextComponent tc) {
                tc.setEnabled(!locked);
            } else {
                field.setEnabled(!locked);
            }
        };
        lockCheck.addItemListener(e -> apply.run());
        apply.run(); // initialize once
    }

    private void loadCustomFont() {
        try {
            Font electrolize = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getResourceAsStream("/fonts/Electrolize-Regular.ttf"))).deriveFont(18f);
            monoFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getResourceAsStream("/fonts/UbuntuSansMono-Regular.ttf"))).deriveFont(20f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(electrolize);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(monoFont);

            // Selective UIManager – proportional for UI, mono for code/logs
            UIManager.put("defaultFont", new FontUIResource(electrolize));
            UIManager.put("monospaceFont", new FontUIResource(monoFont));

            // Propagate changes
            SwingUtilities.updateComponentTreeUI(this);
        } catch (FontFormatException | IOException e) {
            log.error("Failed to load custom font: {}", e.getMessage());
        }
    }

    // Simple dark defaults, so new components pick up colors automatically (no L&F swap)
    private void installDarkDefaults() {
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("TabbedPane.background", BG);
        UIManager.put("TabbedPane.foreground", FG);
        UIManager.put("TabbedPane.contentAreaColor", BG);
        UIManager.put("Label.foreground", FG);
        UIManager.put("CheckBox.foreground", FG);
        UIManager.put("RadioButton.foreground", FG);
        UIManager.put("Button.foreground", FG);
        UIManager.put("Button.background", BG_PANEL);
        UIManager.put("ScrollPane.background", BG);
        UIManager.put("Viewport.background", BG);
        UIManager.put("TextField.background", BG_PANEL);
        UIManager.put("PasswordField.background", BG_PANEL);
        UIManager.put("TextArea.background", BG_PANEL);
        UIManager.put("EditorPane.background", BG_PANEL);
        UIManager.put("TextField.foreground", FG);
        UIManager.put("PasswordField.foreground", FG);
        UIManager.put("TextArea.foreground", FG);
        UIManager.put("EditorPane.foreground", FG);
        UIManager.put("TextField.inactiveForeground", FG_MUTED);
        UIManager.put("PasswordField.inactiveForeground", FG_MUTED);
        UIManager.put("TextArea.inactiveForeground", FG_MUTED);
        UIManager.put("EditorPane.inactiveForeground", FG_MUTED);
    }

    /**
     * Applies a character limit on a specified {@link JTextField}.
     * This method ensures that users cannot input or paste text exceeding the given maximum number of characters.
     * It achieves this by utilizing a {@link DocumentFilter} on the text field's document.
     *
     * @param field    The {@link JTextField} component on which the character limit will be applied.
     *                 If the field is null, the method does nothing.
     * @param maxChars The maximum allowed number of characters in the {@link JTextField}.
     *                 Input exceeding this limit will be truncated or ignored.
     */
    private void installTextLimit(JTextField field, int maxChars) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                int newLen = fb.getDocument().getLength() + string.length();
                if (newLen <= maxChars) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    int allowed = maxChars - fb.getDocument().getLength();
                    if (allowed > 0) {
                        super.insertString(fb, offset, string.substring(0, allowed), attr);
                    }
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    super.replace(fb, offset, length, null, attrs);
                    return;
                }
                int curLen = fb.getDocument().getLength();
                int newLen = curLen - length + text.length();
                if (newLen <= maxChars) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    int allowed = maxChars - (curLen - length);
                    if (allowed > 0) {
                        super.replace(fb, offset, length, text.substring(0, allowed), attrs);
                    }
                }
            }
        });
    }

    // ---------- Helpers ----------

    private JPanel buildAiTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();
        // Fields (80% width field, 20% checkbox)
        // Row 1: Buttons
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        // Use inline subclass to custom-paint the dark background (no reassignment issues)

        startStopServicesButton = new JToggleButton("Start Services") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = BG_PANEL;
                    ButtonModel m = getModel();
                    if (m.isPressed()) base = base.darker();
                    else if (m.isRollover()) base = base.brighter();
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(startStopServicesButton);
        startStopServicesButton.addActionListener(
                e -> {
                    EventBusManager.publish(new ToggleServicesEvent(!isServiceRunning.get()));
                    startStopServicesButton.setEnabled(false);
                }
        );

        showDetailedLog = new JCheckBox("Detailed Log", false);
        showDetailedLog.addActionListener(
                e -> EventBusManager.publish(new ToggleDetailedLogEvent(showDetailedLog.isSelected()))
        );

        toggleStreamingModeCheckBox = new JCheckBox(LABEL_STREAMING_MODE, false);
        toggleStreamingModeCheckBox.addActionListener(
                e -> EventBusManager.publish(new ToggleStreamingModeEvent(toggleStreamingModeCheckBox.isSelected()))
        );

        togglePrivacyModeCheckBox = new JCheckBox(LABEL_STREAMING_MODE, false);
        togglePrivacyModeCheckBox.addActionListener(
                e -> EventBusManager.publish(new TogglePrivacyModeEvent(togglePrivacyModeCheckBox.isSelected()))
        );


        recalibrateAudioButton = new JButton("Recalibrate Audio") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = BG_PANEL;
                    ButtonModel m = getModel();
                    if (m.isPressed()) base = base.darker();
                    else if (m.isRollover()) base = base.brighter();
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        recalibrateAudioButton.setForeground(DISABLED_FG);
        recalibrateAudioButton.addActionListener(e -> {
            EventBusManager.publish(new RecalibrateAudioEvent());
        });
        styleButton(recalibrateAudioButton);
        buttons.add(startStopServicesButton);
        buttons.add(toggleStreamingModeCheckBox);
        buttons.add(togglePrivacyModeCheckBox);
        buttons.add(recalibrateAudioButton);
        buttons.add(showDetailedLog);
        panel.add(new JLabel(" ")); //<-- placeholder
        panel.add(buttons, gbc);

        // Row 2: Logs area fills remaining space
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1; // take the rest of the space
        gbc.fill = GridBagConstraints.BOTH;

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(monoFont);  // Use stored DejaVu only here!
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(BG);
        logArea.setForeground(CONSOLE_FG);
        JScrollPane scroll = new JScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scroll, gbc);

        return panel;
    }

    private JPanel buildPlayerTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        // Row 0: Alternative Name
        addLabel(panel, "Commander Name:", gbc);
        playerAltNameField = new JTextField();
        playerAltNameField.setToolTipText("If you want Elite Intel refer to you by name once in a while.");
        playerAltNameField.setPreferredSize(new Dimension(200, 42));
        addField(panel, playerAltNameField, gbc, 1, 1.0); // full width in the Player tab (no checkbox)

        // Row 1: Title
        nextRow(gbc);
        addLabel(panel, "Title:", gbc);
        playerTitleField = new JTextField();
        playerTitleField.setToolTipText("Optional title. AI will occasionally refer to you by your title. If not provided, title will be based on your highest military rank");
        playerTitleField.setPreferredSize(new Dimension(200, 42));
        addField(panel, playerTitleField, gbc, 1, 1.0);

        // Row 2: Mission Statement (multi-line)
        nextRow(gbc);
        addLabel(panel, "Session Theme:", gbc);
        playerMissionDescription = new JTextField();
        playerMissionDescription.setPreferredSize(new Dimension(200, 42));
        playerMissionDescription.setToolTipText("Session theme description (optional). 'We are bounty hunters' or 'We are deep-space explorers' or 'We are pirates' ");
        installTextLimit(playerMissionDescription, 120);
        addField(panel, playerMissionDescription, gbc, 1, 1.0);

        // Row 3: Journal Directory
        nextRow(gbc);
        addLabel(panel, "Journal Directory:", gbc);
        journalDirField = new JTextField();
        journalDirField.setToolTipText("Custom directory for Elite Dangerous journal files (optional; defaults to standard location if blank)");
        //journalDirField.setText(ConfigManager.getInstance().getJournalPath().toAbsolutePath().toString());
        addField(panel, journalDirField, gbc, 1, 0.8);
        JButton selectJournalDirButton = new JButton("Select...") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = BG_PANEL;
                    ButtonModel m = getModel();
                    if (m.isPressed()) base = base.darker();
                    else if (m.isRollover()) base = base.brighter();
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(selectJournalDirButton);
        selectJournalDirButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select Elite Dangerous Journal Directory");

            // Start in the current known folder (or user home if empty)
            String current = playerSession.getJournalPath().toString();
            if (!current.isBlank()) {
                chooser.setCurrentDirectory(new File(current).getParentFile());
            }

            int result = chooser.showOpenDialog(AppView.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDir = chooser.getSelectedFile();                 // here it is
                String absolutePath = selectedDir.getAbsolutePath();

                playerSession.setJournalPath(absolutePath);   // auto-saves to SQLite
                journalDirField.setText(absolutePath);
            }
        });
        addField(panel, selectJournalDirButton, gbc, 2, 0.2);

        // Row 5: Bindings Directory
        nextRow(gbc);
        addLabel(panel, "Bindings Directory:", gbc);
        bindingsDirField = new JTextField();
        bindingsDirField.setToolTipText("Custom directory for Elite Dangerous key bindings files (optional; defaults to standard location if blank)");
        addField(panel, bindingsDirField, gbc, 1, 0.8);
        JButton selectBindingsDirButton = new JButton("Select...") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = BG_PANEL;
                    ButtonModel m = getModel();
                    if (m.isPressed()) base = base.darker();
                    else if (m.isRollover()) base = base.brighter();
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(selectBindingsDirButton);
        selectBindingsDirButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select Elite Dangerous Bindings Directory");

            // Start in the current known folder (or user home if empty)
            String current = playerSession.getBindingsDir().toString();
            if (!current.isBlank()) {
                chooser.setCurrentDirectory(new File(current).getParentFile());
            }

            int result = chooser.showOpenDialog(AppView.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDir = chooser.getSelectedFile();                 // here it is
                String absolutePath = selectedDir.getAbsolutePath();
                playerSession.setBindingsDir(absolutePath);   // auto-saves to SQLite
                bindingsDirField.setText(absolutePath);
            }
        });
        addField(panel, selectBindingsDirButton, gbc, 2, 0.2);


        // Row 5: Check Boxes
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel checkBoxes = new JPanel(new GridLayout(0, 1, 0, 8));
        checkBoxes.setOpaque(false);

        sendMarketData = new JCheckBox("Send Market Data", false);
        sendMarketData.addActionListener(e -> EventBusManager.publish(new ToggleSendMarketDataEvent(sendMarketData.isSelected())));
        sendShipyardData = new JCheckBox("Send Shipyard Data", false);
        sendShipyardData.addActionListener(e -> EventBusManager.publish(new ToggleSendShipyardDataEvent(sendShipyardData.isSelected())));
        sendOutfitingData = new JCheckBox("Send Outfitting Data", false);
        sendOutfitingData.addActionListener(e -> EventBusManager.publish(new ToggleSendOutfittingDataEvent(sendOutfitingData.isSelected())));
        sendExplorationData = new JCheckBox("Send Exploration Data", false);
        sendExplorationData.addActionListener(e -> EventBusManager.publish(new ToggleSendExplorationDataEvent(sendExplorationData.isSelected())));

        checkBoxes.add(new JLabel(" "));
        checkBoxes.add(new JLabel("This app relies in part on crowd sourced data from pilots like you."));
        checkBoxes.add(new JLabel("Opt In to share Market, Shipyard and Outfitting data from stations you visit to EDDM crowd sourced network."));
        checkBoxes.add(new JLabel("Information is shared anonymously. By default you are Opt-Out (not sharing)"));
        checkBoxes.add(sendMarketData);
        checkBoxes.add(sendShipyardData);
        checkBoxes.add(sendOutfitingData);
        checkBoxes.add(sendExplorationData);
        checkBoxes.add(new JLabel(" "));
        panel.add(checkBoxes, gbc);

        // Row 6: Save button
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);

        JButton savePlayerInfoButton = new JButton("Save Player Configuration") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = BG_PANEL;
                    ButtonModel m = getModel();
                    if (m.isPressed()) base = base.darker();
                    else if (m.isRollover()) base = base.brighter();
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(savePlayerInfoButton);
        savePlayerInfoButton.addActionListener(e -> savePlayerConfig());
        btns.add(savePlayerInfoButton);
        panel.add(btns, gbc);


        // Row 7: Filler area (reserved for future use)
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1; // take the rest of the space
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), gbc);

        return panel;
    }


    private JPanel buildSettingsTab() {
        JPanel settingsTabPanel = new JPanel();
        settingsTabPanel.setLayout(new BoxLayout(settingsTabPanel, BoxLayout.PAGE_AXIS));
        GridBagConstraints gbc = baseGbc();

        /// --------------------------------------------------------------------------------------------------------------------------------------------
        /// Cloud Fields
        nextRow(gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel cloudFields = new JPanel(new GridBagLayout());
        addLabel(cloudFields, "Cloud LLM Key:", gbc);
        llmApiKeyField = new JPasswordField();
        llmApiKeyField.setPreferredSize(new Dimension(200, 42));
        addField(cloudFields, llmApiKeyField, gbc, 1, 0.8);
        llmLockedCheck = new JCheckBox("Locked", true);
        addCheck(cloudFields, llmLockedCheck, gbc);

        nextRow(gbc);
        addLabel(cloudFields, "Cloud STT Key:", gbc);
        sttApiKeyField = new JPasswordField();
        sttApiKeyField.setPreferredSize(new Dimension(200, 42));
        addField(cloudFields, sttApiKeyField, gbc, 1, 0.8);
        sttLockedCheck = new JCheckBox("Locked", true);
        addCheck(cloudFields, sttLockedCheck, gbc);

        nextRow(gbc);
        addLabel(cloudFields, "Cloud TTS Key:", gbc);
        ttsApiKeyField = new JPasswordField();
        ttsApiKeyField.setPreferredSize(new Dimension(200, 42));
        addField(cloudFields, ttsApiKeyField, gbc, 1, 0.8);
        ttsLockedCheck = new JCheckBox("Locked", true);
        addCheck(cloudFields, ttsLockedCheck, gbc);
        cloudFields.setBorder(new LineBorder(ACCENT, 1));
        cloudFields.revalidate();
        addNestedPanel(settingsTabPanel, cloudFields);
        /// --------------------------------------------------------------------------------------------------------------------------------------------


        JPanel localSettingsPanel = new JPanel(new GridBagLayout());
        nextRow(gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;  // Allow horizontal fill if desired
        gbc.weighty = 0.0;  // CRITICAL: No vertical growth!
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nextRow(gbc);
        addLabel(localSettingsPanel, "Local LLM Address:", gbc);
        localLlmAddressField = new JTextField();
        localLlmAddressField.setPreferredSize(new Dimension(200, 42));
        addField(localSettingsPanel, localLlmAddressField, gbc, 1, 0.8);

        nextRow(gbc);
        addLabel(localSettingsPanel, "Local Command LLM:", gbc);
        localLlmModelCommandField = new JTextField();
        localLlmModelCommandField.setPreferredSize(new Dimension(200, 42));
        addField(localSettingsPanel, localLlmModelCommandField, gbc, 1, 0.8);
        useLocalCommandLLMCheck = new JCheckBox("Use", false);
        useLocalCommandLLMCheck.addActionListener(a -> {
            SwingUtilities.invokeLater(this::saveSystemConfig);
        });
        addCheck(localSettingsPanel, useLocalCommandLLMCheck, gbc);

        nextRow(gbc);
        addLabel(localSettingsPanel, "Local Query LLM:", gbc);
        localLlmModelQueryField = new JTextField();
        localLlmModelQueryField.setPreferredSize(new Dimension(200, 42));
        localSettingsPanel.setBorder(new LineBorder(ACCENT, 1));
        localSettingsPanel.revalidate();
        useLocalQueryLLMCheck = new JCheckBox("Use", false);
        useLocalQueryLLMCheck.addActionListener(a -> {
            SwingUtilities.invokeLater(() -> {
                saveSystemConfig();
                //EventBusManager.publish(new RestartServicesEvent());
            });
        });

        addField(localSettingsPanel, localLlmModelQueryField, gbc, 1, 0.8);
        addCheck(localSettingsPanel, useLocalQueryLLMCheck, gbc);


        addNestedPanel(settingsTabPanel, localSettingsPanel);
        /// --------------------------------------------------------------------------------------------------------------------------------------------

        nextRow(gbc);
        addLabel(localSettingsPanel, "Local TTS Address", gbc);
        localTtsAddressField = new JTextField();
        localTtsAddressField.setPreferredSize(new Dimension(200, 42));
        localTtsAddressField.setText("http://localhost:5000/");
        localTtsAddressField.setToolTipText("Local TTS Address");
        useLocalTTSCheck = new JCheckBox("Use", false);
        useLocalTTSCheck.addActionListener(a -> {
            SwingUtilities.invokeLater(() -> {
                saveSystemConfig();
                //EventBusManager.publish(new RestartServicesEvent());
            });
        });

        addField(localSettingsPanel, localTtsAddressField, gbc, 1, 0.8);
        addCheck(localSettingsPanel, useLocalTTSCheck, gbc);

        nextRow(gbc);
        addLabel(localSettingsPanel, "Speech Throttle ", gbc);
        speechSpeedSlider = new JSlider();
        speechSpeedSlider.setMinimum(-100);
        speechSpeedSlider.setMaximum(0);
        speechSpeedSlider.setMajorTickSpacing(25);
        speechSpeedSlider.setMinorTickSpacing(1);
        speechSpeedSlider.setInverted(true);
        speechSpeedSlider.setSnapToTicks(true);
        speechSpeedSlider.setValue(-(int) ((1.0f - systemSession.getSpeechSpeed()) * 100));
        addField(localSettingsPanel, speechSpeedSlider, gbc, 1, 0.8);
        speechSpeedLabel = new JLabel("");
        setSpeedDisplayValue();
        speechSpeedSlider.addChangeListener(e -> SwingUtilities.invokeLater(() -> {
            EventBusManager.publish(
                    new SpeechSpeedChangeEvent(
                            Math.abs(100 + speechSpeedSlider.getValue()) / 100f
                    )
            );
            setSpeedDisplayValue();
        }));

        addLabel(localSettingsPanel, speechSpeedLabel, gbc);
        localSettingsPanel.setBorder(new LineBorder(ACCENT, 1));

        addNestedPanel(settingsTabPanel, localSettingsPanel);
        /// --------------------------------------------------------------------------------------------------------------------------------------------

        /// blank
        nextRow(gbc);
        // Row EDSM KEY
        nextRow(gbc);
        addLabel(localSettingsPanel, "EDSM API Key:", gbc);
        edsmKeyField = new JPasswordField();
        edsmKeyField.setPreferredSize(new Dimension(200, 42));
        edsmKeyField.setToolTipText("EDSM API Key");
        addField(localSettingsPanel, edsmKeyField, gbc, 1, 0.8);
        edsmLockedCheck = new JCheckBox("Locked", true);
        addCheck(localSettingsPanel, edsmLockedCheck, gbc);
        localSettingsPanel.setBorder(new LineBorder(ACCENT, 1));

        addNestedPanel(settingsTabPanel, localSettingsPanel);
        /// --------------------------------------------------------------------------------------------------------------------------------------------


        // Row 3: Buttons
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setOpaque(false);
        // Use inline subclass to custom-paint the dark background (no reassignment issues)
        JButton saveSystemButton = new JButton("Save Configuration") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = BG_PANEL;
                    ButtonModel m = getModel();
                    if (m.isPressed()) base = base.darker();
                    else if (m.isRollover()) base = base.brighter();
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(saveSystemButton);
        saveSystemButton.addActionListener(e -> saveSystemConfig());

        updateAppButton = new JButton("Update App") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = BG_PANEL;
                    ButtonModel m = getModel();
                    if (m.isPressed()) base = base.darker();
                    else if (m.isRollover()) base = base.brighter();
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(updateAppButton);
        updateAppButton.setEnabled(false);
        updateAppButton.setText("App is Up to Date");
        updateAppButton.addActionListener(e -> {
            EventBusManager.publish(new AiVoxResponseEvent("Updating. See you soon..."));
            SleepNoThrow.sleep(3000);
            Updater.performUpdateAsync().thenAccept(success -> {
                if (success) {
                    SwingUtilities.invokeLater(() -> System.exit(0));
                }
            });
        });

        JButton restoreDefaultsButton = new JButton("Restore Local LLM Defaults") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = BG_PANEL;
                    ButtonModel m = getModel();
                    if (m.isPressed()) base = base.darker();
                    else if (m.isRollover()) base = base.brighter();
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        styleButton(restoreDefaultsButton);
        restoreDefaultsButton.setEnabled(true);
        restoreDefaultsButton.setText("Restore Local LLM Defaults");
        restoreDefaultsButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            localLlmAddressField.setText("http://localhost:11434");
            localLlmModelCommandField.setText("qwen2.5:14b");
            localLlmModelQueryField.setText("qwen2.5:14b");
            saveSystemConfig();
        }));

        buttons.add(saveSystemButton);
        buttons.add(updateAppButton);
        buttons.add(restoreDefaultsButton);

        settingsTabPanel.add(new JLabel(" "));
        settingsTabPanel.add(buttons);

        // Row 6: Filler area (reserved for future use)
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1; // take the rest of the space
        gbc.fill = GridBagConstraints.BOTH;

        nextRow(gbc);


        return settingsTabPanel;
    }

    private void setSpeedDisplayValue() {
        float speed = speechSpeedSlider.getValue();
        speechSpeedLabel.setText("+" + (100 + speed) + "%");
    }


    /**
     * Recursively applies a dark theme to the specified UI component and its children.
     * This method updates the background, foreground, borders, and other style aspects
     * based on the component type, ensuring a consistent dark palette across the user
     * interface.
     *
     * @param c The root component or container to which the dark theme will be applied.
     *          If the component is null, the method does nothing.
     */
    // Recursively apply background/foreground and add orange outline to inputs/buttons
    private void applyDarkPalette(Component c) {
        if (c == null) return;

        // Base background/foreground
        if (c instanceof JPanel || c instanceof JTabbedPane || c instanceof JScrollPane) {
            c.setBackground(BG);
            c.setForeground(FG);
        } else {
            c.setBackground(c instanceof JTextComponent ? BG_PANEL : BG);
            c.setForeground(FG);
        }

        if (c instanceof JTextArea) {
            c.setBackground(LOG_BG);
            c.setForeground(CONSOLE_FG);
        }

        // Text components
        if (c instanceof JTextComponent tc) {
            tc.setCaretColor(FG);
            tc.setSelectionColor(SEL_BG);
            tc.setSelectedTextColor(SEL_FG);
            tc.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ACCENT, 1, true),
                    new EmptyBorder(6, 8, 6, 8)
            ));
        }

        // Buttons
        if (c instanceof JButton b) {
            b.setBackground(BG_PANEL);
            b.setForeground(FG);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ACCENT, 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        }

        // Checkboxes
        if (c instanceof JCheckBox cb) {
            cb.setBackground(BG);
            cb.setForeground(FG);
        }

        // TabbedPane tweaks
        if (c instanceof JTabbedPane tp) {
            tp.setBackground(BG);
            tp.setForeground(FG);
            tp.setOpaque(true);
        }

        // ScrollPane viewport
        if (c instanceof JScrollPane sp) {
            sp.getViewport().setBackground(BG);
            sp.setBorder(new LineBorder(ACCENT, 1, true));
        }

        // Help pane specific
        if (c instanceof JEditorPane ep) {
            ep.setBackground(Color.WHITE);
            ep.setForeground(Color.BLACK);
        }

        // Title label slightly accent
        if (c == titleLabel) {
            titleLabel.setForeground(FG);
        }

        if (c instanceof Container cont) {
            for (Component child : cont.getComponents()) {
                applyDarkPalette(child);
            }
        }
    }


    @Override
    public void initData() {
        sttApiKeyField.setText(systemSession.getSttApiKey() != null ? systemSession.getSttApiKey() : "");
        llmApiKeyField.setText(systemSession.getAiApiKey() != null ? systemSession.getAiApiKey() : "");
        ttsApiKeyField.setText(systemSession.getTtsApiKey() != null ? systemSession.getTtsApiKey() : "");
        edsmKeyField.setText(systemSession.getEdsmApiKey() != null ? systemSession.getEdsmApiKey() : "");

        localTtsAddressField.setText(playerSession.getLocalTtsAddress() != null ? playerSession.getLocalTtsAddress() : "");
        localLlmAddressField.setText(playerSession.getLocalLlmAddress() != null ? playerSession.getLocalLlmAddress() : "");
        localLlmModelCommandField.setText(systemSession.getLocalLlmCommandModel() != null ? systemSession.getLocalLlmCommandModel() : "");
        localLlmModelQueryField.setText(systemSession.getLocalLlmQueryModel() != null ? systemSession.getLocalLlmQueryModel() : "");

        useLocalCommandLLMCheck.setSelected(systemSession.useLocalCommandLlm());
        useLocalQueryLLMCheck.setSelected(systemSession.useLocalQueryLlm());
        useLocalTTSCheck.setSelected(systemSession.useLocalTTS());

        // Player tab
        playerAltNameField.setText(playerSession.getAlternativeName() != null ? playerSession.getAlternativeName() : "");
        playerTitleField.setText(playerSession.getPlayerTitle() != null ? playerSession.getPlayerTitle() : "");
        playerMissionDescription.setText(playerSession.getPlayerMissionStatement() != null ? playerSession.getPlayerMissionStatement() : "");
        journalDirField.setText(playerSession.getJournalPath().toString());
        bindingsDirField.setText(playerSession.getBindingsDir().toString());

        sendMarketData.setSelected(systemSession.isSendMarketData());
        sendOutfitingData.setSelected(systemSession.isSendOutfittingData());
        sendShipyardData.setSelected(systemSession.isSendShipyardData());
        sendExplorationData.setSelected(systemSession.isSendExplorationData());

        // streaming / privacy checkboxes
        toggleStreamingModeCheckBox.setSelected(systemSession.isStreamingModeOn());
        setupStreamingCheckBox(systemSession.isStreamingModeOn());
        togglePrivacyModeCheckBox.setSelected(systemSession.isStreamingModeOn());
    }


    private void saveSystemConfig() {
        SystemSession s = SystemSession.getInstance();
        s.setSttApiKey(new String(sttApiKeyField.getPassword()));
        s.setAiApiKey(new String(llmApiKeyField.getPassword()));
        s.setTtsApiKey(new String(ttsApiKeyField.getPassword()));
        s.setEdsmApiKey(new String(edsmKeyField.getPassword()));
        playerSession.setLocalTtsAddress(localTtsAddressField.getText());
        playerSession.setLocalLlmAddress(localLlmAddressField.getText());
        systemSession.setLocalLlmCommandModel(localLlmModelCommandField.getText());
        systemSession.setLocalLlmQueryModel(localLlmModelQueryField.getText());
        systemSession.setUseLocalCommandLlm(useLocalCommandLLMCheck.isSelected());
        systemSession.setUseLocalQueryLlm(useLocalQueryLLMCheck.isSelected());
        systemSession.setUseLocalTTS(useLocalTTSCheck.isSelected());
        EventBusManager.publish(new AppLogEvent("System config saved"));
        initData();
    }

    private void savePlayerConfig() {
        PlayerSession p = PlayerSession.getInstance();
        p.setAlternativeName(playerAltNameField.getText());
        p.setPlayerTitle(playerTitleField.getText());
        p.setPlayerMissionStatement(playerMissionDescription.getText());
        p.setJournalPath(journalDirField.getText());
        p.setBindingsDir(bindingsDirField.getText());
        EventBusManager.publish(new AppLogEvent("Player config saved"));
        initData();
    }


    @Override
    public JFrame getUiComponent() {
        return this;
    }

    private GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        return gbc;
    }

    private void addLabel(JPanel panel, JLabel label, GridBagConstraints gbc) {
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);
    }

    private void addLabel(JPanel panel, String text, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(220, 42));
        panel.add(label, gbc);
    }

    private void addField(JPanel panel, JComponent comp, GridBagConstraints gbc, int col, double weightX) {
        gbc.gridx = col;
        gbc.weightx = weightX;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        comp.setPreferredSize(new Dimension(0, comp.getPreferredSize().height)); // allow to grow
        panel.add(comp, gbc);
    }

    private void addCheck(JPanel panel, JCheckBox check, GridBagConstraints gbc) {
        gbc.gridx = 2;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(check, gbc);
    }

    private void addNestedPanel(JPanel parent, JPanel child) {
        parent.add(child);
    }


    private void nextRow(GridBagConstraints gbc) {
        gbc.gridy++;
    }

    // Keep button styling lightweight; painting is handled by the inline subclass above
    private void styleButton(AbstractButton b) {
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setForeground(FG);
        b.setBackground(BG);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
    }

    private void styleTabbedPane(JTabbedPane tp) {
        tp.setOpaque(true);
        tp.setBackground(BG);
        tp.setForeground(FG);
        tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tp.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                // Keep borders minimal
                contentBorderInsets = new Insets(1, 1, 1, 1);
                tabInsets = new Insets(8, 14, 8, 14);
                selectedTabPadInsets = new Insets(1, 1, 1, 1);
                focus = ACCENT;
            }

            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                // Fill the whole tab strip background
                g.setColor(BG);
                g.fillRect(0, 0, tabPane.getWidth(), calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight));
                super.paintTabArea(g, tabPlacement, selectedIndex);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(isSelected ? TAB_SELECTED : TAB_UNSELECTED);
                g.fillRect(x, y, w, h);
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
                if (isSelected) {
                    g.setColor(ACCENT);
                    g.fillRect(x, y + h - 3, w, 3); // accent underline
                }
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Draw a thin accent border around content
                Insets in = getInsets();
                int top = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                int x = in.left;
                int y = in.top + top;
                int w = tabPane.getWidth() - in.left - in.right;
                int h = tabPane.getHeight() - y - in.bottom;
                g.setColor(ACCENT);
                g.drawRect(x, y, Math.max(0, w - 1), Math.max(0, h - 1));
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // no dotted focus ring
            }
        });
    }

    private void setupStreamingCheckBox(Boolean streamingModeOn) {
        toggleStreamingModeCheckBox.setSelected(streamingModeOn);
        toggleStreamingModeCheckBox.setText(streamingModeOn ? "Streaming On" : "Streaming Off");
    }


    @Subscribe public void onServiceStatusEvent(ServicesStateEvent event) {
        SwingUtilities.invokeLater(() -> {
            SleepNoThrow.sleep(1000);
            isServiceRunning.set(event.isRunning());
            startStopServicesButton.setText(event.isRunning() ? "Stop Services" : "Start Services");
            startStopServicesButton.setEnabled(true);
            recalibrateAudioButton.setEnabled(event.isRunning());
            togglePrivacyModeCheckBox.setEnabled(event.isRunning());
            toggleStreamingModeCheckBox.setEnabled(event.isRunning());

        });
    }

    @Subscribe public void onUpdateAvailableEvent(UpdateAvailableEvent event) {
        SwingUtilities.invokeLater(() -> {
            updateAppButton.setEnabled(true);
            updateAppButton.setText("Update Available");
            this.setTitle("New version available.");
        });
    }


    @Subscribe void onClearConsoleEvent(ClearConsoleEvent event){
        logArea.setText("");
    }

}
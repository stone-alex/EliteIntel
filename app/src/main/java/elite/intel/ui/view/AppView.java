package elite.intel.ui.view;

import elite.intel.ai.ConfigManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the main graphical user interface of the application.
 * The `AppView` class organizes and manages user interactions, layouts,
 * and theming for various sections such as system configuration, player
 * configuration, and help documentation.
 */
public class AppView extends JFrame implements PropertyChangeListener, AppViewInterface {

    public static final String LABEL_STREAMING_MODE = "Streaming Mode";
    public static final String LABEL_PRIVACY_MODE = "Privacy Mode";
    // ----- ACTION COMMANDS -----
    // Action commands are used to trigger specific actions in the UI.
    // They are passed to the controller via the actionPerformed() method.
    // The controller can then use the action command to determine what to do.
    //
    // Note: Action commands are not intended to be used for communication between the UI and the controller.
    //       They are only intended to be used for communication between the UI and the model.
    //
    // Action commands are defined here and referenced in the UI code.
    //
    public static final String ACTION_SAVE_USER_CONFIG = "saveUserConfig";
    public static final String ACTION_SAVE_SYSTEM_CONFIG = "saveSystemConfig";
    public static final String ACTION_TOGGLE_SERVICES = "toggleServices";
    public static final String ACTION_TOGGLE_STREAMING_MODE = "toggleStreamingMode";
    public static final String ACTION_TOGGLE_PRIVACY_MODE = "togglePrivacyMode";
    public static final String ACTION_TOGGLE_SYSTEM_LOG = "toggleSystemLog";
    public static final String ACTION_SELECT_JOURNAL_DIR = "selectJournalDir";
    public static final String ACTION_SELECT_BINDINGS_DIR = "selectBindingsDir";
    public static final String ACTION_RECALIBRATE_AUTIO = "recalibrateAudio";
    // ----- END COLORS -----
    // ----- PROPERTY CHANGE EVENTS -----
    // Property change events are used to notify the UI of changes in the model.
    // They are passed to the controller via the propertyChange() method.
    // The controller can then use the property name to determine what to do.
    //
    // Note: Property change events are not intended to be used for communication between the UI and the controller.
    //       They are only intended to be used for communication between the UI and the model.
    //
    // Property change events are defined here and referenced in the UI code.
    //
    public static final String PROPERTY_SYSTEM_CONFIG_UPDATED = "systemConfigUpdated";
    public static final String PROPERTY_LOG_UPDATED = "logUpdated";
    public static final String PROPERTY_USER_CONFIG_UPDATED = "userConfigUpdated";
    public static final String PROPERTY_STREAMING_MODE = "streamingModeUpdated";
    public static final String PROPERTY_PRIVACY_MODE = "privacyModeUpdated";
    public static final String PROPERTY_HELP_MARKDOWN = "helpMarkdownUpdated";
    public static final String PROPERTY_SERVICES_TOGGLE = "servicesToggled";
    // ----- COLORS (adjust to taste) -----
    private static final Color BG = new Color(0x1D1D1D); // base background
    private static final Color BG_PANEL = new Color(0x2B2D30); // panels/inputs background
    private static final Color FG = new Color(0xE6E6E6); // primary text
    private static final Color FG_MUTED = new Color(0xB0B0B0); // secondary text
    private static final Color ACCENT = new Color(0xFF8C00); // orange
    private static final Color SEL_BG = new Color(0x3A3D41); // selection background
    private static final Color TAB_UNSELECTED = new Color(0x2A2C2F);
    private static final Color TAB_SELECTED = new Color(0x33363A);
    private static final Logger log = LoggerFactory.getLogger(AppView.class);
    // Title
    private final JLabel titleLabel;
    private final AtomicInteger typeIndex = new AtomicInteger(0);
    private final StringBuilder typeBuffer = new StringBuilder();
    // System tab components
    private JPasswordField sttApiKeyField;
    private JCheckBox sttLockedCheck;
    private JPasswordField llmApiKeyField;
    private JCheckBox llmLockedCheck;
    private JCheckBox showDetailedLog;
    private JPasswordField ttsApiKeyField;
    private JCheckBox ttsLockedCheck;
    private JButton saveSystemButton;
    private JButton startStopServicesButton;
    private JButton recalibrateAudioButton;
    private JCheckBox toggleStreamingModeCheckBox;
    private JCheckBox togglePrivacyModeCheckBox;
    private JTextArea logArea;
    private JPasswordField systemYouTubeStreamKey;
    private JCheckBox ytLockedCheck;
    private JTextField systemYouTubeStreamUrl;
    // Player tab components
    private JTextField playerAltNameField;
    private JTextField playerTitleField;
    private JTextField playerMissionDescription;
    private JTextField journalDirField;


    // ---------- Public API ----------
    private JTextField bindingsDirField;
    private JButton savePlayerInfoButton;
    private JButton selectJournalDirButton;
    private JButton selectBindingsDirButton;
    // Help tab
    private JEditorPane helpPane; // HTML rendering
    private Timer logTypewriterTimer;
    private String pendingLogText;

    public AppView() {
        super("Elite Intel");
        // Load and apply custom font before any other UI setup
        loadCustomFont();
        // Apply dark theme defaults
        installDarkDefaults();
        EventBusManager.register(this);

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/elite-logo.png")));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 1000));
        setSize(new Dimension(1200, 1000));
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
        tabs.addTab("System", buildSystemTab());
        tabs.addTab("Player", buildPlayerTab());
        //tabs.addTab("Help", buildHelpTab()); // <- Help tab disabled for now
        root.add(tabs, BorderLayout.CENTER);
        applyDarkPalette(getContentPane());

        //initial state
        bindLock(sttLockedCheck, sttApiKeyField);
        bindLock(llmLockedCheck, llmApiKeyField);
        bindLock(ttsLockedCheck, ttsApiKeyField);
        bindLock(ytLockedCheck, systemYouTubeStreamKey);
        toggleStreamingModeCheckBox.setEnabled(false);//enabled when services start
        toggleStreamingModeCheckBox.setToolTipText("Prevent AI from processing unless you prefix your command or query with word 'computer'");
        toggleStreamingModeCheckBox.setText(LABEL_STREAMING_MODE);
        toggleStreamingModeCheckBox.setForeground(Color.GREEN);

        togglePrivacyModeCheckBox.setEnabled(false); // enabled when services start
        togglePrivacyModeCheckBox.setToolTipText("Temporary disable Speech to Text completely");
        togglePrivacyModeCheckBox.setText(LABEL_PRIVACY_MODE);
        togglePrivacyModeCheckBox.setForeground(Color.GREEN);

        journalDirField.setEditable(false);
        journalDirField.setPreferredSize(new Dimension(200, 42));
        bindingsDirField.setEditable(false);
        bindingsDirField.setPreferredSize(new Dimension(200, 42));
    }


    private void loadCustomFont() {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/Electrolize-Regular.ttf")).deriveFont(20f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            UIManager.put("defaultFont", font);

            // Update all component fonts
            for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet()) {
                if (entry.getValue() instanceof FontUIResource) {
                    UIManager.put(entry.getKey(), new FontUIResource(font));
                }
            }
        } catch (FontFormatException | IOException e) {
            log.error("Failed to load custom font: {}", e.getMessage());
        }
    }


    // Simple dark defaults so new components pick up colors automatically (no L&F swap)
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

    private JPanel buildSystemTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        // Fields (80% width field, 20% checkbox)

        // Row 0: Grok API Key
        nextRow(gbc);
        addLabel(panel, "xAI or Open AI Key:", gbc, 0);
        llmApiKeyField = new JPasswordField();
        llmApiKeyField.setPreferredSize(new Dimension(200, 42));
        addField(panel, llmApiKeyField, gbc, 1, 0.8);
        llmLockedCheck = new JCheckBox("Locked", true);
        addCheck(panel, llmLockedCheck, gbc, 2, 0.2);

        // Row 1: STT Key
        nextRow(gbc);
        addLabel(panel, "Google STT Key:", gbc, 0);
        sttApiKeyField = new JPasswordField();
        sttApiKeyField.setPreferredSize(new Dimension(200, 42));
        addField(panel, sttApiKeyField, gbc, 1, 0.8);
        sttLockedCheck = new JCheckBox("Locked", true);
        addCheck(panel, sttLockedCheck, gbc, 2, 0.2);

        // Row 2: TTS Key
        nextRow(gbc);
        addLabel(panel, "Google TTS Key:", gbc, 0);
        ttsApiKeyField = new JPasswordField();
        ttsApiKeyField.setPreferredSize(new Dimension(200, 42));
        addField(panel, ttsApiKeyField, gbc, 1, 0.8);
        ttsLockedCheck = new JCheckBox("Locked", true);
        addCheck(panel, ttsLockedCheck, gbc, 2, 0.2);

        // Row YouTube KEY
        nextRow(gbc);
        addLabel(panel, "YouTube API Key:", gbc, 0);
        systemYouTubeStreamKey = new JPasswordField();
        systemYouTubeStreamKey.setPreferredSize(new Dimension(200, 42));
        systemYouTubeStreamKey.setToolTipText("YouTube API Key");
        addField(panel, systemYouTubeStreamKey, gbc, 1, 0.8);
        ytLockedCheck = new JCheckBox("Locked", true);
        addCheck(panel, ytLockedCheck, gbc, 2, 0.2);

        // Row YouTube URL
        nextRow(gbc);
        addLabel(panel, "YouTube Stream:", gbc, 0);
        systemYouTubeStreamUrl = new JTextField();
        systemYouTubeStreamUrl.setPreferredSize(new Dimension(200, 42));
        systemYouTubeStreamUrl.setToolTipText("Enter your Stream URL if you want TTS for chat");
        addField(panel, systemYouTubeStreamUrl, gbc, 1, 1.0);


        // Row 3: Buttons
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        // Use inline subclass to custom-paint the dark background (no reassignment issues)
        saveSystemButton = new JButton("Save Configuration") {
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
        saveSystemButton.setActionCommand(ACTION_SAVE_SYSTEM_CONFIG);


        startStopServicesButton = new JButton("Start Services") {
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
        startStopServicesButton.setActionCommand(ACTION_TOGGLE_SERVICES);
        styleButton(startStopServicesButton);

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

        recalibrateAudioButton.setActionCommand(ACTION_RECALIBRATE_AUTIO);
        styleButton(recalibrateAudioButton);


        showDetailedLog = new JCheckBox("Detailed Log", true);
        showDetailedLog.setActionCommand(ACTION_TOGGLE_SYSTEM_LOG);

        toggleStreamingModeCheckBox = new JCheckBox(LABEL_STREAMING_MODE, false);
        toggleStreamingModeCheckBox.setActionCommand(ACTION_TOGGLE_STREAMING_MODE);

        togglePrivacyModeCheckBox = new JCheckBox(LABEL_STREAMING_MODE, false);
        togglePrivacyModeCheckBox.setActionCommand(ACTION_TOGGLE_PRIVACY_MODE);


        buttons.add(saveSystemButton);
        buttons.add(startStopServicesButton);
        buttons.add(recalibrateAudioButton);
        buttons.add(showDetailedLog);
        buttons.add(toggleStreamingModeCheckBox);
        buttons.add(togglePrivacyModeCheckBox);

        panel.add(buttons, gbc);

        // Row 4+: Logs area fills remaining space
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1; // take the rest of the space
        gbc.fill = GridBagConstraints.BOTH;

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new JTextArea().getFont()); // default mono is optional; keep default look
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scroll, gbc);

        return panel;
    }

    private JPanel buildPlayerTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        // Row 0: Alternative Name
        addLabel(panel, "Alternative Name:", gbc, 0);
        playerAltNameField = new JTextField();
        playerAltNameField.setToolTipText("If your name can't be pronounced properly by TTS, provide an alternative name here. (optional) ");
        playerAltNameField.setPreferredSize(new Dimension(200, 42));
        addField(panel, playerAltNameField, gbc, 1, 1.0); // full width in Player tab (no checkbox)

        // Row 1: Title
        nextRow(gbc);
        addLabel(panel, "Title:", gbc, 0);
        playerTitleField = new JTextField();
        playerTitleField.setToolTipText("Optional title. AI will occasionally refer to you by your title. If not provided, title will be based on your highest military rank");
        playerTitleField.setPreferredSize(new Dimension(200, 42));
        addField(panel, playerTitleField, gbc, 1, 1.0);

        // Row 2: Mission Statement (multi-line)
        nextRow(gbc);
        addLabel(panel, "Session Theme:", gbc, 0);
        playerMissionDescription = new JTextField();
        playerMissionDescription.setPreferredSize(new Dimension(200, 42));
        playerMissionDescription.setToolTipText("Session theme description (optional). 'We are bounty hunters' or 'We are deep-space explorers' or 'We are pirates' ");
        installTextLimit(playerMissionDescription, 120);
        addField(panel, playerMissionDescription, gbc, 1, 1.0);

        // Row 4: Journal Directory
        nextRow(gbc);
        addLabel(panel, "Journal Directory:", gbc, 0);
        journalDirField = new JTextField();
        journalDirField.setToolTipText("Custom directory for Elite Dangerous journal files (optional; defaults to standard location if blank)");
        //journalDirField.setText(ConfigManager.getInstance().getJournalPath().toAbsolutePath().toString());
        addField(panel, journalDirField, gbc, 1, 0.8);
        selectJournalDirButton = new JButton("Select...") {
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
        selectJournalDirButton.setActionCommand(ACTION_SELECT_JOURNAL_DIR);
        addField(panel, selectJournalDirButton, gbc, 2, 0.2);

        // Row 5: Bindings Directory
        nextRow(gbc);
        addLabel(panel, "Bindings Directory:", gbc, 0);
        bindingsDirField = new JTextField();
        bindingsDirField.setToolTipText("Custom directory for Elite Dangerous key bindings files (optional; defaults to standard location if blank)");
        addField(panel, bindingsDirField, gbc, 1, 0.8);
        selectBindingsDirButton = new JButton("Select...") {
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
        selectBindingsDirButton.setActionCommand(ACTION_SELECT_BINDINGS_DIR);
        addField(panel, selectBindingsDirButton, gbc, 2, 0.2);

        // Row 5: Save button
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);

        savePlayerInfoButton = new JButton("Save Player Configuration") {
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
        savePlayerInfoButton.setActionCommand(ACTION_SAVE_USER_CONFIG);

        btns.add(savePlayerInfoButton);
        panel.add(btns, gbc);

        // Row 6: Filler area (reserved for future use)
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1; // take the rest of the space
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), gbc);

        return panel;
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
            c.setBackground(Color.BLACK);
            c.setForeground(ACCENT);
        }

        // Text components
        if (c instanceof JTextComponent tc) {
            tc.setCaretColor(FG);
            tc.setSelectionColor(SEL_BG);
            tc.setSelectedTextColor(FG);
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
    
    public void addActionListener(ActionListener l) {
        if (saveSystemButton != null) saveSystemButton.addActionListener(l);
        if (startStopServicesButton != null) startStopServicesButton.addActionListener(l);
        if (recalibrateAudioButton != null) recalibrateAudioButton.addActionListener(l);
        if (savePlayerInfoButton != null) savePlayerInfoButton.addActionListener(l);
        if (toggleStreamingModeCheckBox != null) toggleStreamingModeCheckBox.addActionListener(l);
        if (togglePrivacyModeCheckBox != null) togglePrivacyModeCheckBox.addActionListener(l);
        if (showDetailedLog != null) showDetailedLog.addActionListener(l);
        if (selectJournalDirButton != null) selectJournalDirButton.addActionListener(l);
        if (selectBindingsDirButton != null) selectBindingsDirButton.addActionListener(l);
    }

    @Override
    public JFrame getUiComponent() {
        return this;
    }

    @Override public void setupControlls(boolean isServiceRunning) {
        toggleStreamingModeCheckBox.setEnabled(isServiceRunning);
        togglePrivacyModeCheckBox.setEnabled(isServiceRunning);
        startStopServicesButton.setText(isServiceRunning ? "Stop Service" : "Start Service");
    }

    // System config I/O
    public void setSystemConfig(Map<String, String> cfg) {
        if (cfg == null) return;
        if (sttApiKeyField != null) {
            sttApiKeyField.setText(cfg.getOrDefault(ConfigManager.STT_API_KEY, ""));
            sttLockedCheck.setSelected(!cfg.getOrDefault(ConfigManager.STT_API_KEY, "").isEmpty());
        }
        if (llmApiKeyField != null) {
            llmApiKeyField.setText(cfg.getOrDefault(ConfigManager.AI_API_KEY, ""));
            llmLockedCheck.setSelected(!cfg.getOrDefault(ConfigManager.AI_API_KEY, "").isEmpty());
        }
        if (ttsApiKeyField != null) {
            ttsApiKeyField.setText(cfg.getOrDefault(ConfigManager.TTS_API_KEY, ""));
            ttsLockedCheck.setSelected(!cfg.getOrDefault(ConfigManager.TTS_API_KEY, "").isEmpty());
        }
        if (systemYouTubeStreamKey != null) {
            systemYouTubeStreamKey.setText(cfg.getOrDefault(ConfigManager.YT_API_KEY, ""));
            ytLockedCheck.setSelected(!cfg.getOrDefault(ConfigManager.YT_API_KEY, "").isEmpty());
        }
        if (systemYouTubeStreamUrl != null) {
            systemYouTubeStreamUrl.setText(cfg.getOrDefault(ConfigManager.YT_URL, ""));
        }
    }

    @Override
    public void displaySystemConfig(Map<String, String> cfg) {
        for (Map.Entry<String, String> entry : cfg.entrySet()) {
            switch (entry.getKey()) {
                case ConfigManager.TTS_API_KEY:
                    sttApiKeyField.setText(entry.getValue());
                    sttLockedCheck.setSelected(!cfg.getOrDefault(ConfigManager.STT_API_KEY, "").isEmpty());
                    break;
                case ConfigManager.AI_API_KEY:
                    llmApiKeyField.setText(entry.getValue());
                    llmLockedCheck.setSelected(!cfg.getOrDefault(ConfigManager.AI_API_KEY, "").isEmpty());
                    break;
                case ConfigManager.STT_API_KEY:
                    ttsApiKeyField.setText(entry.getValue());
                    ttsLockedCheck.setSelected(!cfg.getOrDefault(ConfigManager.TTS_API_KEY, "").isEmpty());
                    break;
                case ConfigManager.YT_API_KEY:
                    systemYouTubeStreamKey.setText(entry.getValue());
                    ytLockedCheck.setSelected(!cfg.getOrDefault(ConfigManager.YT_API_KEY, "").isEmpty());
                    break;
                case ConfigManager.YT_URL:
                    systemYouTubeStreamUrl.setText(entry.getValue());
                    break;
            }
        }
    }

    @Override
    public void displayUserConfig(Map<String, String> cfg) {
        for (Map.Entry<String, String> entry : cfg.entrySet()) {
            switch (entry.getKey()) {
                case ConfigManager.PLAYER_ALTERNATIVE_NAME:
                    playerAltNameField.setText(entry.getValue());
                    break;
                case ConfigManager.PLAYER_CUSTOM_TITLE:
                    playerTitleField.setText(entry.getValue());
                    break;
                case ConfigManager.PLAYER_MISSION_STATEMENT:
                    playerMissionDescription.setText(entry.getValue());
                    break;
                case ConfigManager.BINDINGS_DIR:
                    bindingsDirField.setText(ConfigManager.getInstance().getBindingsPath().toAbsolutePath().toString());
                case ConfigManager.JOURNAL_DIR:
                    journalDirField.setText(ConfigManager.getInstance().getJournalPath().toAbsolutePath().toString());
                    break;
            }
        }

    }

    @Override
    public void displayHelp(String helpText) {
        setHelpMarkdown(helpText);
    }

    /**
     * Retrieves the system configuration input provided by the user.
     * This method collects and returns the configuration details
     * populated in the user interface fields for system-related settings.
     * It includes API keys for TTS, STT, and AI features, if available.
     *
     * @return A map containing system configuration key-value pairs,
     * such as API keys for various services. If a field is not
     * populated, its corresponding key will not be included in
     * the map.
     */
    public Map<String, String> getSystemConfigInput() {
        Map<String, String> cfg = new HashMap<>();
        if (sttApiKeyField != null) cfg.put(ConfigManager.TTS_API_KEY, new String(sttApiKeyField.getPassword()));
        if (llmApiKeyField != null) cfg.put(ConfigManager.AI_API_KEY, new String(llmApiKeyField.getPassword()));
        if (ttsApiKeyField != null) cfg.put(ConfigManager.STT_API_KEY, new String(ttsApiKeyField.getPassword()));
        if (systemYouTubeStreamUrl != null) cfg.put(ConfigManager.YT_URL, systemYouTubeStreamUrl.getText());
        if (systemYouTubeStreamKey != null) cfg.put(ConfigManager.YT_API_KEY, systemYouTubeStreamKey.getText());
        return cfg;
    }

    /**
     * Updates the user configuration by applying the provided key-value mappings
     * to the relevant user interface fields.
     * This method updates specific fields like the player's alternative name,
     * title, and mission description based on the configuration values.
     * If a field is null, no updates are made to that field.
     *
     * @param cfg A map containing user configuration key-value pairs.
     *            The keys should correspond to predefined configuration constants
     *            (e.g., PLAYER_ALTERNATIVE_NAME, PLAYER_TITLE, PLAYER_MISSION_STATEMENT).
     *            If the map is null, the method does nothing.
     */
    // User config I/O
    public void setUserConfig(Map<String, String> cfg) {
        if (cfg == null) return;
        if (playerAltNameField != null) playerAltNameField.setText(cfg.getOrDefault(ConfigManager.PLAYER_ALTERNATIVE_NAME, ""));
        if (playerTitleField != null) playerTitleField.setText(cfg.getOrDefault(ConfigManager.PLAYER_CUSTOM_TITLE, ""));
        if (playerMissionDescription != null) playerMissionDescription.setText(cfg.getOrDefault(ConfigManager.PLAYER_MISSION_STATEMENT, ""));
        if (systemYouTubeStreamUrl != null) {
            systemYouTubeStreamUrl.setText(cfg.getOrDefault(ConfigManager.YT_URL, ""));
        }
        if (journalDirField != null) journalDirField.setText(cfg.getOrDefault(ConfigManager.JOURNAL_DIR, ""));
        if (bindingsDirField != null) bindingsDirField.setText(cfg.getOrDefault(ConfigManager.BINDINGS_DIR, ""));
    }

    /**
     * Retrieves the user configuration input provided through the user interface.
     * This method collects and returns the configuration details entered in specific
     * fields such as the player's alternative name, title, and mission description.
     * If certain fields are not populated, their corresponding keys will not be included
     * in the resulting map.
     *
     * @return A map containing key-value pairs for user configuration. The keys correspond
     * to predefined configuration identifiers (e.g., PLAYER_ALTERNATIVE_NAME,
     * PLAYER_TITLE, PLAYER_MISSION_STATEMENT). If no input is provided, the map
     * will only include non-null field values.
     */
    public Map<String, String> getUserConfigInput() {
        Map<String, String> cfg = new HashMap<>();
        if (playerAltNameField != null) cfg.put(ConfigManager.PLAYER_ALTERNATIVE_NAME, playerAltNameField.getText());
        if (playerTitleField != null) cfg.put(ConfigManager.PLAYER_CUSTOM_TITLE, playerTitleField.getText());
        if (playerMissionDescription != null) cfg.put(ConfigManager.PLAYER_MISSION_STATEMENT, playerMissionDescription.getText());
        if (journalDirField != null) cfg.put(ConfigManager.JOURNAL_DIR, journalDirField.getText());
        if (bindingsDirField != null) cfg.put(ConfigManager.BINDINGS_DIR, bindingsDirField.getText());
        return cfg;
    }

    /**
     * Updates the content of the log area with the given text. If the current text
     * in the log area and the new text share a common prefix, only the differing
     * part of the text is gradually updated using an animated effect.
     *
     * @param text the new log text to display in the log area; if null, the method does nothing
     */
//TODO: Analyze this for re-paint bug
    public void setLogText(String text) {
        if (logArea == null || text == null) return;

        String current = logArea.getText();
        if (current.equals(text)) return;

        // Cancel any ongoing
        if (logTypewriterTimer != null) {
            logTypewriterTimer.stop();
        }

        int prefix = 0;
        int min = Math.min(current.length(), text.length());
        while (prefix < min && current.charAt(prefix) == text.charAt(prefix)) prefix++;

        // Init
        typeBuffer.setLength(0);
        typeBuffer.append(text, 0, prefix);
        typeIndex.set(prefix);
        pendingLogText = text;

        // Reuse or create timer
        if (logTypewriterTimer == null) {
            logTypewriterTimer = new Timer(7, e -> {
                int idx = typeIndex.get();
                if (idx < pendingLogText.length()) {
                    typeBuffer.append(pendingLogText.charAt(idx));
                    logArea.setText(typeBuffer.toString());
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                    typeIndex.incrementAndGet();
                } else {
                    logTypewriterTimer.stop();
                }
            });
        } else {
            logTypewriterTimer.restart();
        }
        logTypewriterTimer.start();
    }
    
    public void setHelpMarkdown(String markdown) {
        if (helpPane == null) return;
        helpPane.setText(markdown);
        helpPane.setCaretPosition(0);
    }
    // ----- END ACTION COMMANDS -----

    /**
     * Creates and initializes a basic instance of {@link GridBagConstraints} with default styling
     * and layout parameters. This method configures the constraints such that components are
     * aligned to the west, have no fill, and include uniform insets. The grid coordinates and the
     * weight for both axes are set to zero.
     *
     * @return A {@link GridBagConstraints} object preconfigured with default grid position, padding,
     * alignment, and other layout properties.
     */
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

    private void addLabel(JPanel panel, String text, GridBagConstraints gbc, int col) {
        gbc.gridx = col;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(text), gbc);
    }

    private void addField(JPanel panel, JComponent comp, GridBagConstraints gbc, int col, double weightX) {
        gbc.gridx = col;
        gbc.weightx = weightX;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        comp.setPreferredSize(new Dimension(0, comp.getPreferredSize().height)); // allow grow
        panel.add(comp, gbc);
    }

    private void addCheck(JPanel panel, JCheckBox check, GridBagConstraints gbc, int col, double weightX) {
        gbc.gridx = col;
        gbc.weightx = weightX;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(check, gbc);
    }

    private void nextRow(GridBagConstraints gbc) {
        gbc.gridy++;
    }

    // Keep button styling lightweight; painting is handled by the inline subclass above
    private void styleButton(JButton b) {
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setForeground(FG);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
    }

    /**
     * Styles the specified JTabbedPane with a dark theme and custom tab rendering.
     * Configures the tab background, accent underline for the selected tab,
     * minimal borders, and scroll tab layout policy.
     *
     * @param tp the JTabbedPane to be styled
     */
    // Paint dark tabs with ACCENT underline for the selected tab
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PROPERTY_SYSTEM_CONFIG_UPDATED)) {
            setSystemConfig((Map<String, String>) evt.getNewValue());
        } else if (evt.getPropertyName().equals(PROPERTY_USER_CONFIG_UPDATED)) {
            setUserConfig((Map<String, String>) evt.getNewValue());
        } else if (evt.getPropertyName().equals(PROPERTY_LOG_UPDATED)) {
            setLogText((String) evt.getNewValue());
        } else if (evt.getPropertyName().equals(PROPERTY_STREAMING_MODE)) {
            Boolean streamingModeOn = (Boolean) evt.getNewValue();
            setupStreamingCheckBox(streamingModeOn);
        } else if (evt.getPropertyName().equals(PROPERTY_PRIVACY_MODE)) {
            Boolean privacyModeOn = (Boolean) evt.getNewValue();
            togglePrivacyModeCheckBox.setSelected(privacyModeOn);
            togglePrivacyModeCheckBox.setForeground(privacyModeOn ? Color.RED : Color.GREEN);
            togglePrivacyModeCheckBox.setText(privacyModeOn ? "Turn Mic Off" : "Turn Mic On");
        } else if (evt.getPropertyName().equals(PROPERTY_HELP_MARKDOWN)) {
            setHelpMarkdown((String) evt.getNewValue());
        } else if (evt.getPropertyName().equals(PROPERTY_SERVICES_TOGGLE)) {
            toggleStreamingModeCheckBox.setEnabled((Boolean) evt.getNewValue());
            togglePrivacyModeCheckBox.setEnabled((Boolean) evt.getNewValue());
            setupStreamingCheckBox(SystemSession.getInstance().isStreamingModeOn());
        }
    }

    private void setupStreamingCheckBox(Boolean streamingModeOn) {
        toggleStreamingModeCheckBox.setSelected(streamingModeOn);
        toggleStreamingModeCheckBox.setForeground(streamingModeOn ? Color.RED : Color.GREEN);
        toggleStreamingModeCheckBox.setText(streamingModeOn ? "Streaming On" : "Streaming Off");
    }
}
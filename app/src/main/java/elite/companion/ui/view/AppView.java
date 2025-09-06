package elite.companion.ui.view;

import elite.companion.comms.ConfigManager;

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
import java.util.HashMap;
import java.util.Map;

/**
 * AppView is the main UI frame of the application responsible for displaying and controlling
 * various aspects of the system and user configuration, logs, and help content. It extends
 * JFrame and implements the AppViewInterface for standardized interaction.
 * <p>
 * The class includes predefined UI styles for a dark theme and organizes its interface
 * into multiple tabs: System, Player, and Help. It also allows for integration with external
 * listeners to handle UI control actions.
 * <p>
 * Features:
 * - Displays and updates system configuration, user configuration, logs, and help content.
 * - Supports user inputs for various text fields and checkboxes.
 * - Integrates with action listeners for handling events.
 * - Implements custom styles for a cohesive dark-themed UI.
 */
public class AppView extends JFrame implements PropertyChangeListener, AppViewInterface {

    // ----- COLORS (adjust to taste) -----
    private static final Color BG = new Color(0x1D1D1D); // base background
    private static final Color BG_PANEL = new Color(0x2B2D30); // panels/inputs background
    private static final Color FG = new Color(0xE6E6E6); // primary text
    private static final Color FG_MUTED = new Color(0xB0B0B0); // secondary text
    private static final Color ACCENT = new Color(0xFF8C00); // orange
    private static final Color SEL_BG = new Color(0x3A3D41); // selection background
    private static final Color TAB_UNSELECTED = new Color(0x2A2C2F);
    private static final Color TAB_SELECTED = new Color(0x33363A);
    // ----- END COLORS -----


    // Title
    private final JLabel titleLabel;

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
    private JCheckBox togglePrivacyModeCheckBox;
    private JTextArea logArea;

    // Player tab components
    private JTextField playerAltNameField;
    private JTextField playerTitleField;
    private JTextField playerMissionDescription; // was JTextArea
    private JButton savePlayerInfoButton;

    // Help tab
    private JEditorPane helpPane; // HTML rendering

    /**
     * Constructs a new instance of the AppView GUI.
     * This class represents the primary user interface for the application, initialized with
     * a specific layout and various UI components such as tabs, labels, buttons, and checkboxes.
     * <p>
     * Key Functionalities:
     * - Sets up the window's size, location, and appearance, including a default dark theme
     * and global font settings.
     * - Configures the main UI components including a tabbed panel for System, Player,
     * and Help sections.
     * - Applies custom styling and properties such as font sizes, colors, and borders to enhance
     * readability and usability.
     * - Sets initial states and bindings for UI elements related to user preferences and
     * streaming mode controls.
     * <p>
     * Behavior:
     * - The constructor initializes the application's primary JFrame with specific
     * properties like dimensions, a window icon, and close operation behavior.
     * - It prepares UI components, applies consistent dark styling, and binds
     * interactive elements for enabling/disabling features based on user actions.
     * <p>
     * Constraints:
     * - Requires initialization of style defaults and font settings on Swing components
     * prior to component creation to apply them consistently.
     * - Assumes a dark theme and utilizes a predefined set of color and font resources for
     * styling UI elements.
     */
    public AppView() {
        super("Elite Companion (version 9-5-2025)");
        // Apply a readable, Windows-friendly font to the entire UI BEFORE creating components
        installUIFont(getPlatformDefaultFont(18f)); // Adjust base size here (e.g., 14f, 15f, 16f)
        installDarkDefaults(); // set dark defaults before components are created


        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/elite-logo.png")));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 900));
        setSize(new Dimension(1200, 900));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        titleLabel = new JLabel("Elite Companion", SwingConstants.CENTER);
        // Make the title a bit larger than the base
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, titleLabel.getFont().getSize2D() + 3f));
        root.add(titleLabel, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        styleTabbedPane(tabs);
        tabs.addTab("System", buildSystemTab());
        tabs.addTab("Player", buildPlayerTab());
        tabs.addTab("Help", buildHelpTab());
        root.add(tabs, BorderLayout.CENTER);
        applyDarkPalette(getContentPane());

        //initial state
        bindLock(sttLockedCheck, sttApiKeyField);
        bindLock(llmLockedCheck, llmApiKeyField);
        bindLock(ttsLockedCheck, ttsApiKeyField);
        togglePrivacyModeCheckBox.setEnabled(false);//enabled when services start
        togglePrivacyModeCheckBox.setToolTipText("Prevent AI from listening to everything on/off, prefix commands with word 'computer' or AI voice name");
        togglePrivacyModeCheckBox.setText("Toggle Privacy Mode");
        togglePrivacyModeCheckBox.setForeground(Color.GREEN);
    }

    private JPanel buildSystemTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        // Fields (80% width field, 20% checkbox)

        // Row 0: Grok API Key
        nextRow(gbc);
        addLabel(panel, "LLM API Key:", gbc, 0);
        llmApiKeyField = new JPasswordField();
        addField(panel, llmApiKeyField, gbc, 1, 0.8);
        llmLockedCheck = new JCheckBox("Locked", true);
        addCheck(panel, llmLockedCheck, gbc, 2, 0.2);

        // Row 1: STT Key
        nextRow(gbc);
        addLabel(panel, "Speech to Text API Key:", gbc, 0);
        sttApiKeyField = new JPasswordField();
        addField(panel, sttApiKeyField, gbc, 1, 0.8);
        sttLockedCheck = new JCheckBox("Locked", true);
        addCheck(panel, sttLockedCheck, gbc, 2, 0.2);

        // Row 2: TTS Key
        nextRow(gbc);
        addLabel(panel, "Text to Speech API Key:", gbc, 0);
        ttsApiKeyField = new JPasswordField();
        addField(panel, ttsApiKeyField, gbc, 1, 0.8);
        ttsLockedCheck = new JCheckBox("Locked", true);
        addCheck(panel, ttsLockedCheck, gbc, 2, 0.2);

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
            @Override protected void paintComponent(Graphics g) {
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
            @Override protected void paintComponent(Graphics g) {
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

        showDetailedLog = new JCheckBox("Show Detailed Log", false);
        showDetailedLog.setActionCommand(ACTION_TOGGLE_SYSTEM_LOG);

        togglePrivacyModeCheckBox = new JCheckBox("Toggle Privacy Mode", false);
        togglePrivacyModeCheckBox.setActionCommand(ACTION_TOGGLE_STREAMING_MODE);

        buttons.add(saveSystemButton);
        buttons.add(startStopServicesButton);
        buttons.add(showDetailedLog);
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
        addField(panel, playerAltNameField, gbc, 1, 1.0); // full width in Player tab (no checkbox)

        // Row 1: Title
        nextRow(gbc);
        addLabel(panel, "Title:", gbc, 0);
        playerTitleField = new JTextField();
        playerTitleField.setToolTipText("Optional title. AI will occasionally refer to you by your title. If not provided, title will be based on your highest military rank");
        addField(panel, playerTitleField, gbc, 1, 1.0);

        // Row 2: Mission Statement (multi-line)
        nextRow(gbc);
        addLabel(panel, "Session Theme:", gbc, 0);
        playerMissionDescription = new JTextField();
        playerMissionDescription.setToolTipText("Session theme description (optional). 'We are bounty hunters' or 'We are deep-space explorers' or 'We are pirates' ");
        installTextLimit(playerMissionDescription, 120);
        addField(panel, playerMissionDescription, gbc, 1, 1.0);

        // Row 3: Save button
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);

        savePlayerInfoButton = new JButton("Save Player Configuration") {
            @Override protected void paintComponent(Graphics g) {
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

        // Row 4: Filler area (reserved for future use)
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1; // take the rest of the space
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), gbc);

        return panel;
    }

    // Install a global UI font across all Swing components
    private static void installUIFont(Font base) {
        if (base == null) return;
        FontUIResource f = new FontUIResource(base);
        for (Object key : UIManager.getLookAndFeelDefaults().keySet()) {
            Object val = UIManager.get(key);
            if (val instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    // Simple dark defaults so new components pick up colors automatically (no L&F swap)
    private static void installDarkDefaults() {
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

    // Choose a sensible Windows-default font, fallback to Dialog if not available
    private static Font getPlatformDefaultFont(@SuppressWarnings("SameParameterValue") /*configured above for convenience*/ float size) {
        String[] candidates = {
                "Segoe UI",       // Windows 10/11
                "Tahoma",         // Older Windows
                "Verdana",        // Widely available
                "Dialog"          // Cross-platform fallback
        };
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String name : candidates) {
            for (String fam : ge.getAvailableFontFamilyNames()) {
                if (fam.equalsIgnoreCase(name)) {
                    return new Font(fam, Font.PLAIN, Math.round(size));
                }
            }
        }
        return new Font("Dialog", Font.PLAIN, Math.round(size));
    }


    private JPanel buildHelpTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));

        helpPane = new JEditorPane();
        helpPane.setEditable(false);
        helpPane.setContentType("text/html");
        helpPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JScrollPane scroll = new JScrollPane(helpPane);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }


    /**
     * Binds a lock checkbox to a specific field, allowing the field's state
     * (enabled, editable, or read-only) to be toggled based on the checkbox selection.
     *
     * @param lockCheck                The checkbox that controls the locking mechanism.
     * @param field                    The UI component whose state will be controlled by the checkbox.
     *                                 Supports JTextComponent and other JComponent types.
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


    // ---------- Public API ----------

    public void addActionListener(ActionListener l) {
        if (saveSystemButton != null) saveSystemButton.addActionListener(l);
        if (startStopServicesButton != null) startStopServicesButton.addActionListener(l);
        if (savePlayerInfoButton != null) savePlayerInfoButton.addActionListener(l);
        if (togglePrivacyModeCheckBox != null) togglePrivacyModeCheckBox.addActionListener(l);
        if (showDetailedLog != null) showDetailedLog.addActionListener(l);
    }

    @Override public JFrame getUiComponent() {
        return this;
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
    }

    @Override public void displaySystemConfig(Map<String, String> cfg) {
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
            }
        }
    }

    @Override public void displayUserConfig(Map<String, String> cfg) {
        for (Map.Entry<String, String> entry : cfg.entrySet()) {
            switch (entry.getKey()) {
                case ConfigManager.PLAYER_ALTERNATIVE_NAME:
                    playerAltNameField.setText(entry.getValue());
                    break;
                case ConfigManager.PLAYER_TITLE:
                    playerTitleField.setText(entry.getValue());
                    break;
                case ConfigManager.PLAYER_MISSION_STATEMENT:
                    playerMissionDescription.setText(entry.getValue());
                    break;
            }
        }

    }

    @Override public void displayHelp(String helpText) {
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
        if (playerTitleField != null) playerTitleField.setText(cfg.getOrDefault(ConfigManager.PLAYER_TITLE, ""));
        if (playerMissionDescription != null) playerMissionDescription.setText(cfg.getOrDefault(ConfigManager.PLAYER_MISSION_STATEMENT, ""));
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
        if (playerTitleField != null) cfg.put(ConfigManager.PLAYER_TITLE, playerTitleField.getText());
        if (playerMissionDescription != null) cfg.put(ConfigManager.PLAYER_MISSION_STATEMENT, playerMissionDescription.getText());
        return cfg;
    }

    /**
     * Updates the log text displayed in the log area of the user interface.
     * If the provided text is null, the log area will be cleared.
     * The method also ensures that the caret (text cursor) is positioned
     * at the end of the updated text.
     *
     * @param text The new text to display in the log area. If null, the log area will be cleared.
     */
    // Logs and Help
    public void setLogText(String text) {
        if (logArea != null) {
            logArea.setText(text == null ? "" : text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    public void setHelpMarkdown(String markdown) {
        if (helpPane == null) return;
        helpPane.setText(markdown);
        helpPane.setCaretPosition(0);
    }

    // ---------- Helpers ----------

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
    private static void installTextLimit(JTextField field, int maxChars) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
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
    public static final String ACTION_TOGGLE_SYSTEM_LOG = "toggleSystemLog";
    // ----- END ACTION COMMANDS -----


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
    public static final String PROPERTY_HELP_MARKDOWN = "helpMarkdownUpdated";
    public static final String PROPERTY_SERVICES_TOGGLE = "servicesToggled";


    @Override public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PROPERTY_SYSTEM_CONFIG_UPDATED)) {
            setSystemConfig((Map<String, String>) evt.getNewValue());
        } else if (evt.getPropertyName().equals(PROPERTY_USER_CONFIG_UPDATED)) {
            setUserConfig((Map<String, String>) evt.getNewValue());
        } else if (evt.getPropertyName().equals(PROPERTY_LOG_UPDATED)) {
            setLogText((String) evt.getNewValue());
        } else if (evt.getPropertyName().equals(PROPERTY_STREAMING_MODE)) {
            Boolean streamingModeOn = (Boolean) evt.getNewValue();
            togglePrivacyModeCheckBox.setSelected(streamingModeOn);
            togglePrivacyModeCheckBox.setForeground(streamingModeOn ? Color.RED : Color.GREEN);
            togglePrivacyModeCheckBox.setText(streamingModeOn ? "I am Ignoring You (say computer to talk to me)" : "I am Listening to your every word");
        } else if (evt.getPropertyName().equals(PROPERTY_HELP_MARKDOWN)) {
            setHelpMarkdown((String) evt.getNewValue());
        } else if (evt.getPropertyName().equals(PROPERTY_SERVICES_TOGGLE)) {
            togglePrivacyModeCheckBox.setEnabled((Boolean) evt.getNewValue());
        }
    }
}
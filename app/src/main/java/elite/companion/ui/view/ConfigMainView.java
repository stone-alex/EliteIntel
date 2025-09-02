package elite.companion.ui.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal, code-only main window for configuration.
 * - 1200x900
 * - 10px padding around content
 * - Title at top center
 * - Three tabs: System, Player, Help
 *
 * Exposed methods:
 * - setSystemConfig(Map), getSystemConfigInput()
 * - setUserConfig(Map), getUserConfigInput()
 * - setLogText(String)
 * - setHelpMarkdown(String)
 * - setServicesRunning(boolean)
 * - addActionListener(ActionListener)  // action commands: saveSystemConfig, toggleServices, saveUserConfig
 */
public class ConfigMainView extends JFrame implements PropertyChangeListener, IView {

    // ----- COLORS (adjust to taste) -----
    private static final Color BG        = new Color(0x1E1F22); // base background
    private static final Color BG_PANEL  = new Color(0x2B2D30); // panels/inputs background
    private static final Color FG        = new Color(0xE6E6E6); // primary text
    private static final Color FG_MUTED  = new Color(0xB0B0B0); // secondary text
    private static final Color ACCENT    = new Color(0xFF8C00); // orange
    private static final Color SEL_BG    = new Color(0x3A3D41); // selection background
    private static final Color TAB_UNSELECTED = new Color(0x2A2C2F);
    private static final Color TAB_SELECTED   = new Color(0x33363A);


    // Title
    private JLabel titleLabel;

    // Tabs
    private JTabbedPane tabs;

    // System tab components
    private JPasswordField googleApiKeyField;
    private JCheckBox googleLockedCheck;
    private JPasswordField grokApiKeyField;
    private JCheckBox grokLockedCheck;
    private JPasswordField edsmApiKeyField;
    private JCheckBox edsmLockedCheck;
    private JButton saveSystemButton;
    private JButton startStopServicesButton;
    private JTextArea logArea;

    // Player tab components
    private JTextField altNameField;
    private JTextField titleField;
    private JTextField missionStatementField; // was JTextArea
    private JButton saveUserButton;

    // Help tab
    private JEditorPane helpPane; // HTML rendering

    public ConfigMainView() {
        super("Elite Companion");
        // Apply a readable, Windows-friendly font to the entire UI BEFORE creating components
        installUIFont(getPlatformDefaultFont(15f)); // Adjust base size here (e.g., 14f, 15f, 16f)
        installDarkDefaults(); // set dark defaults before components are created



        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/elite-logo.png")));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setSize(new Dimension(1200, 900));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        titleLabel = new JLabel("Elite Companion", SwingConstants.CENTER);
        // Make title a bit larger than base
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, titleLabel.getFont().getSize2D() + 3f));
        root.add(titleLabel, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        styleTabbedPane(tabs);
        tabs.addTab("System", buildSystemTab());
        tabs.addTab("Player", buildPlayerTab());
        tabs.addTab("Help", buildHelpTab());
        root.add(tabs, BorderLayout.CENTER);
        applyDarkPalette(getContentPane());
    }

    private JPanel buildSystemTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        // Fields (80% width field, 20% checkbox)
        // Row 0: Google API Key
        addLabel(panel, "Google API Key:", gbc, 0);
        googleApiKeyField = new JPasswordField();
        addField(panel, googleApiKeyField, gbc, 1, 0.8);
        googleLockedCheck = new JCheckBox("Locked", true);
        addCheck(panel, googleLockedCheck, gbc, 2, 0.2);

        // Row 1: Grok API Key
        nextRow(gbc);
        addLabel(panel, "Grok API Key:", gbc, 0);
        grokApiKeyField = new JPasswordField();
        addField(panel, grokApiKeyField, gbc, 1, 0.8);
        grokLockedCheck = new JCheckBox("Locked", true);
        addCheck(panel, grokLockedCheck, gbc, 2, 0.2);

        // Row 2: EDSM API Key
        nextRow(gbc);
        addLabel(panel, "EDSM API Key:", gbc, 0);
        edsmApiKeyField = new JPasswordField();
        addField(panel, edsmApiKeyField, gbc, 1, 0.8);
        edsmLockedCheck = new JCheckBox("Locked", true);
        addCheck(panel, edsmLockedCheck, gbc, 2, 0.2);

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
                } finally { g2.dispose(); }
                super.paintComponent(g);
            }
        };
        styleButton(saveSystemButton);
        saveSystemButton.setActionCommand("saveSystemConfig");



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
                } finally { g2.dispose(); }
                super.paintComponent(g);
            }
        };
        startStopServicesButton.setActionCommand("toggleServices");
        styleButton(startStopServicesButton);

        buttons.add(saveSystemButton);
        buttons.add(startStopServicesButton);

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
        altNameField = new JTextField();
        addField(panel, altNameField, gbc, 1, 1.0); // full width in Player tab (no checkbox)

        // Row 1: Title
        nextRow(gbc);
        addLabel(panel, "Title:", gbc, 0);
        titleField = new JTextField();
        addField(panel, titleField, gbc, 1, 1.0);

        // Row 2: Mission Statement (multi-line)
        nextRow(gbc);
        addLabel(panel, "Current Mission:", gbc, 0);
        missionStatementField = new JTextField();
        installTextLimit(missionStatementField, 120);
        JScrollPane missionScroll = new JScrollPane(missionStatementField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        addField(panel, missionScroll, gbc, 1, 1.0);

        // Row 3: Save button
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);

        saveUserButton = new JButton("Save Player Configuration") {
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
                } finally { g2.dispose(); }
                super.paintComponent(g);
            }
        };
        styleButton(saveUserButton);
        saveUserButton.setActionCommand("saveUserConfig");;
        btns.add(saveUserButton);
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
            ep.setBackground(BG_PANEL);
            ep.setForeground(FG);
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
    private static Font getPlatformDefaultFont(float size) {
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

    // ---------- Public API ----------

    public void addActionListener(ActionListener l) {
        if (saveSystemButton != null) saveSystemButton.addActionListener(l);
        if (startStopServicesButton != null) startStopServicesButton.addActionListener(l);
        if (saveUserButton != null) saveUserButton.addActionListener(l);
    }

    @Override public JFrame getUiComponent() {
        return this;
    }

    public void setServicesRunning(boolean running) {
        if (startStopServicesButton != null) {
            startStopServicesButton.setText(running ? "Stop Services" : "Start Services");
        }
    }

    // System config I/O
    public void setSystemConfig(Map<String, String> cfg) {
        if (cfg == null) return;
        if (googleApiKeyField != null) googleApiKeyField.setText(cfg.getOrDefault("google_api_key", ""));
        if (grokApiKeyField != null) grokApiKeyField.setText(cfg.getOrDefault("grok_key", ""));
        if (edsmApiKeyField != null) edsmApiKeyField.setText(cfg.getOrDefault("edsm_key", ""));
    }

    @Override public void displaySystemConfig(Map<String, String> config) {

    }

    @Override public void displayUserConfig(Map<String, String> config) {

    }

    @Override public void displayLog(String log) {

    }

    @Override public void displayHelp(String helpText) {

    }

    public Map<String, String> getSystemConfigInput() {
        Map<String, String> cfg = new HashMap<>();
        if (googleApiKeyField != null) cfg.put("google_api_key", new String(googleApiKeyField.getPassword()));
        if (grokApiKeyField != null) cfg.put("grok_key", new String(grokApiKeyField.getPassword()));
        if (edsmApiKeyField != null) cfg.put("edsm_key", new String(edsmApiKeyField.getPassword()));
        cfg.put("google_api_key_locked", String.valueOf(googleLockedCheck != null && googleLockedCheck.isSelected()));
        cfg.put("grok_key_locked", String.valueOf(grokLockedCheck != null && grokLockedCheck.isSelected()));
        cfg.put("edsm_key_locked", String.valueOf(edsmLockedCheck != null && edsmLockedCheck.isSelected()));
        return cfg;
    }

    // User config I/O
    public void setUserConfig(Map<String, String> cfg) {
        if (cfg == null) return;
        if (altNameField != null) altNameField.setText(cfg.getOrDefault("alternative_name", ""));
        if (titleField != null) titleField.setText(cfg.getOrDefault("title", ""));
        if (missionStatementField != null) missionStatementField.setText(cfg.getOrDefault("mission_statement", ""));
    }

    public Map<String, String> getUserConfigInput() {
        Map<String, String> cfg = new HashMap<>();
        if (altNameField != null) cfg.put("alternative_name", altNameField.getText());
        if (titleField != null) cfg.put("title", titleField.getText());
        if (missionStatementField != null) cfg.put("mission_statement", missionStatementField.getText());
        return cfg;
    }

    // Logs and Help
    public void setLogText(String text) {
        if (logArea != null) {
            logArea.setText(text == null ? "" : text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    public void setHelpMarkdown(String markdown) {
        if (helpPane == null) return;
        String html = markdownToHtml(markdown == null ? "" : markdown);
        helpPane.setText(html);
        helpPane.setCaretPosition(0);
    }

    // ---------- Helpers ----------

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

    // Very small Markdown -> HTML converter for common cases
    private static String markdownToHtml(String md) {
        String html = md
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        // Headers
        html = html.replaceAll("(?m)^######\\s*(.*)$", "<h6>$1</h6>");
        html = html.replaceAll("(?m)^#####\\s*(.*)$", "<h5>$1</h5>");
        html = html.replaceAll("(?m)^####\\s*(.*)$", "<h4>$1</h4>");
        html = html.replaceAll("(?m)^###\\s*(.*)$", "<h3>$1</h3>");
        html = html.replaceAll("(?m)^##\\s*(.*)$", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^#\\s*(.*)$", "<h1>$1</h1>");

        // Bold/italic
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        html = html.replaceAll("__(.+?)__", "<b>$1</b>");
        html = html.replaceAll("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)", "<i>$1</i>");
        html = html.replaceAll("(?<!_)_(?!_)(.+?)(?<!_)_(?!_)", "<i>$1</i>");

        // Inline code
        html = html.replaceAll("`([^`]+)`", "<code>$1</code>");

        // Links [text](url)
        html = html.replaceAll("\\[(.+?)\\]\\((https?://[^\\s)]+)\\)", "<a href=\"$2\">$1</a>");

        // Lists
        html = html.replaceAll("(?m)^\\s*[-*]\\s+(.+)$", "<li>$1</li>");
        html = html.replaceAll("(?s)(<li>.*?</li>)", "<ul>$1</ul>");

        // Paragraphs (very naive)
        html = "<html><body style='font-family:Segoe UI, Sans-Serif;'>" +
                html.replaceAll("(?m)^(?!<h\\d>|<ul>|<li>|</ul>|<p>|</p>|<code>|</code>|<b>|</b>|<i>|</i>|<a |</a>)(.+)$", "<p>$1</p>") +
                "</body></html>";
        return html;
    }

    // Quick demo launcher (optional)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConfigMainView view = new ConfigMainView();
            view.setVisible(true);
        });
    }

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








    @Override public void propertyChange(PropertyChangeEvent evt) {

    }




}
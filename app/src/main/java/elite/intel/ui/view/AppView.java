package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.controller.AiTabController;
import elite.intel.ui.event.ShipProfileChangedEvent;
import elite.intel.ui.event.SystemShutDownEvent;
import elite.intel.ui.event.UpdateAvailableEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class AppView extends JFrame implements AppViewInterface {

    private static final Logger log = LoggerFactory.getLogger(AppView.class);
    private static final String ICON_AI = "/images/ai.png";
    private static final String ICON_PLAYER = "/images/controller.png";
    private static final String ICON_SETTINGS = "/images/settings.png";
    private static final String RELEASE_ICON = "/images/release.png";
    private static final String MANUAL_ICON = "/images/manual.png";

    private final SystemSession systemSession = SystemSession.getInstance();
    private final AiTabPanel aiTabPanel;
    private final PlayerTabPanel playerTabPanel;
    private final SettingsTabPanel settingsTabPanel;
    private final MarkdownViewPanel releaseNotesPanel;
    private final MarkdownViewPanel userManualPanel;

    public AppView() {
        super("--");
        setTitle("Elite Intel " + systemSession.readVersionFromResources());
        Font monoFont = loadCustomFont();
        installDarkDefaults();
        EventBusManager.register(this);

        setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/images/elite-logo.png")));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 500));
        setSize(new Dimension(1200, 900));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        JLabel titleLabel = new JLabel("Elite Intel", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, titleLabel.getFont().getSize2D() + 3f));
        root.add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        AppTheme.styleTabbedPane(tabs);

        ImageIcon aiIcon = scaledIcon(ICON_AI);
        ImageIcon playerIcon = scaledIcon(ICON_PLAYER);
        ImageIcon settingsIcon = scaledIcon(ICON_SETTINGS);
        ImageIcon releaseIcon = scaledIcon(RELEASE_ICON);
        ImageIcon manualIcon = scaledIcon(MANUAL_ICON);

        aiTabPanel = new AiTabPanel(monoFont);
        playerTabPanel = new PlayerTabPanel();
        settingsTabPanel = new SettingsTabPanel();
        releaseNotesPanel = new MarkdownViewPanel("release-notes.md");
        userManualPanel = new MarkdownViewPanel("user-manual.md");

        tabs.addTab("Ai", aiIcon, aiTabPanel);
        tabs.addTab("Player", playerIcon, playerTabPanel);
        tabs.addTab("Settings", settingsIcon, settingsTabPanel);
        tabs.addTab("Manual", manualIcon, userManualPanel);
        tabs.addTab("Release Notes", releaseIcon, releaseNotesPanel);

        root.add(tabs, BorderLayout.CENTER);
        AppTheme.applyDarkPalette(getContentPane());

        new AiTabController(aiTabPanel);

        initData();
    }

    private ImageIcon scaledIcon(String resource) {
        return new ImageIcon(
                new ImageIcon(Objects.requireNonNull(getClass().getResource(resource)))
                        .getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    }

    @Override
    public void initData() {
        settingsTabPanel.initData();
        playerTabPanel.initData();
        aiTabPanel.initData(systemSession.isSleepingModeOn());
    }

    @Subscribe
    public void onShipProfileChangedEvent(ShipProfileChangedEvent event) {
        SwingUtilities.invokeLater(this::initData);
    }

    @Override
    public JFrame getUiComponent() {
        return this;
    }

    @Subscribe
    public void onUpdateAvailableEvent(UpdateAvailableEvent event) {
        SwingUtilities.invokeLater(() -> setTitle("New version available."));
    }

    @Subscribe
    public void onSystemShutdownEvent(SystemShutDownEvent event) {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            System.exit(0);
        });
    }

    private Font loadCustomFont() {
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 20);
        try {
            Font electrolize = Font.createFont(Font.TRUETYPE_FONT,
                            Objects.requireNonNull(getClass().getResourceAsStream("/fonts/Electrolize-Regular.ttf")))
                    .deriveFont(18f);
            mono = Font.createFont(Font.TRUETYPE_FONT,
                            Objects.requireNonNull(getClass().getResourceAsStream("/fonts/UbuntuSansMono-Regular.ttf")))
                    .deriveFont(20f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(electrolize);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(mono);
            UIManager.put("defaultFont", new FontUIResource(electrolize));
            UIManager.put("monospaceFont", new FontUIResource(mono));
            SwingUtilities.updateComponentTreeUI(this);
        } catch (FontFormatException | IOException e) {
            log.error("Failed to load custom font: {}", e.getMessage());
        }
        return mono;
    }

    private void installDarkDefaults() {
        UIManager.put("Panel.background", AppTheme.BG);
        UIManager.put("OptionPane.background", AppTheme.BG);
        UIManager.put("TabbedPane.background", AppTheme.BG);
        UIManager.put("TabbedPane.foreground", AppTheme.FG);
        UIManager.put("TabbedPane.contentAreaColor", AppTheme.BG);
        UIManager.put("Label.foreground", AppTheme.FG);
        UIManager.put("CheckBox.foreground", AppTheme.FG);
        UIManager.put("RadioButton.foreground", AppTheme.BUTTON_BG);
        UIManager.put("Button.foreground", AppTheme.BUTTON_FG);
        UIManager.put("Button.background", AppTheme.BUTTON_BG);
        UIManager.put("ScrollPane.background", AppTheme.BG);
        UIManager.put("Viewport.background", AppTheme.BG);
        UIManager.put("TextField.background", AppTheme.BG_PANEL);
        UIManager.put("PasswordField.background", AppTheme.BG_PANEL);
        UIManager.put("TextArea.background", AppTheme.BG_PANEL);
        UIManager.put("EditorPane.background", AppTheme.BG_PANEL);
        UIManager.put("TextField.foreground", AppTheme.FG);
        UIManager.put("PasswordField.foreground", AppTheme.FG);
        UIManager.put("TextArea.foreground", AppTheme.FG);
        UIManager.put("EditorPane.foreground", AppTheme.FG);
        UIManager.put("TextField.inactiveForeground", AppTheme.FG_MUTED);
        UIManager.put("PasswordField.inactiveForeground", AppTheme.FG_MUTED);
        UIManager.put("TextArea.inactiveForeground", AppTheme.FG_MUTED);
        UIManager.put("EditorPane.inactiveForeground", AppTheme.FG_MUTED);
    }
}

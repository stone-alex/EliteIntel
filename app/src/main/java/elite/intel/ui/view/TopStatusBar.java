package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.actions.customcommand.CustomCommandRegistry;
import elite.intel.db.dao.ShipDao;
import elite.intel.db.managers.ShipManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.ActiveShipChangedEvent;
import elite.intel.ui.event.BindingsSummaryChangedEvent;
import elite.intel.ui.event.CommanderChangedEvent;
import elite.intel.ui.event.CustomCommandsSummaryChangedEvent;
import elite.intel.ui.event.KeymapSyncStateChangedEvent;
import elite.intel.ui.event.LlmUsageEvent;
import elite.intel.ui.event.ServicesStateEvent;
import elite.intel.ui.event.SleepWakeStateChangedEvent;
import elite.intel.ui.event.TTSProviderChangedEvent;

import javax.swing.*;
import java.awt.*;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Live telemetry strip displayed at the top of the main application window.
 * Subscribes directly to focused state-change events; all badge mutations run on the EDT.
 * Call {@link #dispose()} when discarding to unregister event subscriptions.
 */
public class TopStatusBar extends HudPanel {

    private JLabel cmdrValue;
    private JLabel shipValue;
    private StatusBadge sttBadge;
    private StatusBadge llmBadge;
    private StatusBadge ttsBadge;
    private StatusBadge bindingsBadge;
    private StatusBadge commandsBadge;
    private StatusBadge keymapBadge;

    private boolean servicesRunning;
    /** True when the voice gate is closed (app is sleeping); false when the app is listening. */
    private boolean sleeping;
    private String lastLlmProvider;

    /**
     * Creates and registers the telemetry bar; reads initial state from application singletons.
     *
     * @param appName        localized application name
     * @param version        application version displayed as build metadata
     * @param servicesRunning initial services state
     */
    public TopStatusBar(String appName, String version, boolean servicesRunning) {
        super(new BorderLayout(AppTheme.HUD_GAP, 0), AppTheme.HUD_CYAN, Variant.FLAT);
        this.servicesRunning = servicesRunning;
        this.sleeping = SystemSession.getInstance().isSleepingModeOn();
        setPreferredSize(new Dimension(0, AppTheme.HUD_TOP_BAR_HEIGHT));
        setMinimumSize(new Dimension(0, AppTheme.HUD_TOP_BAR_HEIGHT));
        setBackground(AppTheme.HUD_SHELL_BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.HUD_BORDER_DIM),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        add(buildBrand(appName, version), BorderLayout.WEST);
        add(buildSegments(), BorderLayout.CENTER);

        EventBusManager.register(this);
    }

    /** Unregisters all event subscriptions. Must be called when this component is discarded. */
    public void dispose() {
        EventBusManager.unregister(this);
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    @Subscribe
    public void onServicesState(ServicesStateEvent event) {
        SwingUtilities.invokeLater(() -> {
            servicesRunning = event.isRunning();
            refreshSttBadge();
            refreshLlmBadge();
            refreshTtsBadge();
        });
    }

    @Subscribe
    public void onSleepWakeStateChanged(SleepWakeStateChangedEvent event) {
        SwingUtilities.invokeLater(() -> {
            sleeping = event.sleeping();
            refreshSttBadge();
        });
    }

    @Subscribe
    public void onLlmUsage(LlmUsageEvent event) {
        SwingUtilities.invokeLater(() -> {
            lastLlmProvider = event.provider();
            refreshLlmBadge();
        });
    }

    @Subscribe
    public void onTtsProviderChanged(TTSProviderChangedEvent event) {
        SwingUtilities.invokeLater(this::refreshTtsBadge);
    }

    @Subscribe
    public void onCommanderChanged(CommanderChangedEvent event) {
        SwingUtilities.invokeLater(() -> cmdrValue.setText(resolvedOrDash(event.commanderName())));
    }

    @Subscribe
    public void onActiveShipChanged(ActiveShipChangedEvent event) {
        SwingUtilities.invokeLater(() -> shipValue.setText(resolvedOrDash(event.shipName())));
    }

    @Subscribe
    public void onBindingsSummaryChanged(BindingsSummaryChangedEvent event) {
        SwingUtilities.invokeLater(() -> {
            String text = getText("hud.bindings.summary", event.missing(), event.connected());
            StatusBadge.State state = event.missing() > 0 ? StatusBadge.State.STANDBY : StatusBadge.State.OK;
            bindingsBadge.setStatus(text, state);
        });
    }

    @Subscribe
    public void onCustomCommandsSummaryChanged(CustomCommandsSummaryChangedEvent event) {
        SwingUtilities.invokeLater(() ->
                commandsBadge.setStatus(getText("hud.commands.summary", event.count()), StatusBadge.State.INFO));
    }

    @Subscribe
    public void onKeymapSyncStateChanged(KeymapSyncStateChangedEvent event) {
        SwingUtilities.invokeLater(() -> keymapBadge.setStatus(
                event.inSync() ? getText("hud.keymap.inSync") : getText("hud.keymap.modified"),
                event.inSync() ? StatusBadge.State.OK : StatusBadge.State.STANDBY));
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    private JComponent buildBrand(String appName, String version) {
        String name = appName == null ? "" : appName.toUpperCase();
        String ver = version == null || version.isBlank() ? "UNKNOWN" : version;
        JLabel label = new JLabel(name + "  " + ver);
        label.setForeground(AppTheme.FG);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setAlignmentY(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JPanel buildSegments() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

        // ── CMDR ──────────────────────────────────────────────────────────────
        row.add(sep());
        row.add(keyLabel(getText("hud.cmdr")));
        row.add(hgap());
        cmdrValue = valueLabel(initCommanderName(), AppTheme.ACCENT);
        row.add(cmdrValue);

        // ── Ship ──────────────────────────────────────────────────────────────
        row.add(sep());
        row.add(keyLabel(getText("hud.ship")));
        row.add(hgap());
        shipValue = valueLabel(initShipName(), AppTheme.HUD_CYAN_SOFT);
        row.add(shipValue);

        // ── STT ───────────────────────────────────────────────────────────────
        row.add(sep());
        row.add(keyLabel(getText("hud.stt")));
        row.add(hgap());
        sttBadge = new StatusBadge(getText("hud.state.standby"), StatusBadge.State.STANDBY);
        refreshSttBadge(); // apply initial sleeping/listening/standby state
        row.add(sttBadge);

        // ── LLM ───────────────────────────────────────────────────────────────
        row.add(sep());
        row.add(keyLabel(getText("hud.llm")));
        row.add(hgap());
        llmBadge = new StatusBadge(
                servicesRunning ? getText("hud.state.active") : getText("hud.state.standby"),
                servicesRunning ? StatusBadge.State.OK : StatusBadge.State.STANDBY);
        row.add(llmBadge);

        // ── TTS ───────────────────────────────────────────────────────────────
        row.add(sep());
        row.add(keyLabel(getText("hud.tts")));
        row.add(hgap());
        ttsBadge = new StatusBadge(getText("hud.state.standby"), StatusBadge.State.STANDBY);
        refreshTtsBadge();
        row.add(ttsBadge);

        // ── Bindings ──────────────────────────────────────────────────────────
        row.add(sep());
        row.add(keyLabel(getText("hud.bindings")));
        row.add(hgap());
        bindingsBadge = new StatusBadge("—", StatusBadge.State.INFO);
        row.add(bindingsBadge);

        // ── Commands ──────────────────────────────────────────────────────────
        row.add(sep());
        row.add(keyLabel(getText("hud.commands")));
        row.add(hgap());
        int initCount = CustomCommandRegistry.getInstance().getCustomCommands().size();
        commandsBadge = new StatusBadge(getText("hud.commands.summary", initCount), StatusBadge.State.INFO);
        row.add(commandsBadge);

        // ── Keymap ────────────────────────────────────────────────────────────
        row.add(sep());
        row.add(keyLabel(getText("hud.keymap")));
        row.add(hgap());
        keymapBadge = new StatusBadge("—", StatusBadge.State.INFO);
        row.add(keymapBadge);

        row.add(hgap());
        return row;
    }

    // ── Badge refresh helpers ─────────────────────────────────────────────────

    private void refreshSttBadge() {
        if (!servicesRunning) {
            sttBadge.setStatus(getText("hud.state.standby"), StatusBadge.State.STANDBY);
        } else if (sleeping) {
            sttBadge.setStatus(getText("hud.state.sleeping"), StatusBadge.State.STANDBY);
        } else {
            sttBadge.setStatus(getText("hud.state.listening"), StatusBadge.State.OK);
        }
    }

    private void refreshLlmBadge() {
        if (!servicesRunning) {
            llmBadge.setStatus(getText("hud.state.standby"), StatusBadge.State.STANDBY);
        } else if (lastLlmProvider != null && !lastLlmProvider.isBlank()) {
            llmBadge.setStatus(lastLlmProvider, StatusBadge.State.OK);
        } else {
            llmBadge.setStatus(getText("hud.state.active"), StatusBadge.State.OK);
        }
    }

    private void refreshTtsBadge() {
        if (!servicesRunning) {
            ttsBadge.setStatus(getText("hud.state.standby"), StatusBadge.State.STANDBY);
        } else {
            boolean local = SystemSession.getInstance().useLocalTTS();
            ttsBadge.setStatus(
                    local ? getText("hud.tts.local") : getText("hud.tts.cloud"),
                    StatusBadge.State.OK);
        }
    }

    // ── Initial state reads ───────────────────────────────────────────────────

    private static String initCommanderName() {
        try {
            String name = PlayerSession.getInstance().getConfiguredPlayerName();
            // getConfiguredPlayerName() returns "Commander" as the default fallback — treat as unknown
            return (name != null && !name.isBlank() && !"Commander".equals(name)) ? name : "—";
        } catch (Exception ignored) {
            return "—";
        }
    }

    private static String initShipName() {
        try {
            ShipDao.Ship ship = ShipManager.getInstance().getShip();
            return (ship != null && ship.getShipName() != null && !ship.getShipName().isBlank())
                    ? ship.getShipName() : "—";
        } catch (Exception ignored) {
            return "—";
        }
    }

    // ── Component factories ───────────────────────────────────────────────────

    private static JLabel sep() {
        JLabel s = new JLabel("  |  ");
        s.setForeground(AppTheme.HUD_DISABLED);
        s.setFont(s.getFont().deriveFont(Font.PLAIN, AppTheme.HUD_FONT_XS));
        s.setAlignmentY(Component.CENTER_ALIGNMENT);
        return s;
    }

    private static JLabel keyLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase() + ":");
        l.setForeground(AppTheme.FG_MUTED);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, AppTheme.HUD_FONT_XS));
        l.setAlignmentY(Component.CENTER_ALIGNMENT);
        return l;
    }

    private static JLabel valueLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(l.getFont().deriveFont(Font.BOLD, AppTheme.HUD_FONT_SM));
        l.setAlignmentY(Component.CENTER_ALIGNMENT);
        return l;
    }

    private static Component hgap() {
        return Box.createHorizontalStrut(4);
    }

    private static String resolvedOrDash(String value) {
        return (value != null && !value.isBlank()) ? value : "—";
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(AppTheme.HUD_SHELL_BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}

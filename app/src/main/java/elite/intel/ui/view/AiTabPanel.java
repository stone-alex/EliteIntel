package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.brain.actions.customcommand.CustomCommandRegistry;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.*;
import elite.intel.ui.telemetry.LlmSessionStatsSnapshot;
import elite.intel.ui.telemetry.LlmSessionStatsTracker;
import elite.intel.util.SleepNoThrow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;
import static elite.intel.ui.view.AppTheme.*;

public class AiTabPanel extends JPanel {

    JCheckBox toggleWakeWordOnOff;

    private JToggleButton startStopServicesButton;
    private JButton recalibrateAudioButton;
    private final AtomicBoolean isServiceRunning = new AtomicBoolean(false);

    private HudLogArea userPanel;
    private HudLogArea aiPanel;
    private HudLogArea systemPanel;

    // QUICK STATUS badges
    private StatusBadge sttBadge;
    private StatusBadge llmBadge;
    private StatusBadge ttsBadge;
    private StatusBadge bindingsBadge;
    private StatusBadge commandsBadge;
    private StatusBadge keymapBadge;
    private boolean sleeping;
    private String lastLlmProvider;

    // SYSTEM SUMMARY telemetry blocks
    private HudTelemetryBlock modelBlock;
    private HudTelemetryBlock sessionTimeBlock;
    private HudTelemetryBlock tokensBlock;
    private HudTelemetryBlock tphBlock;
    private HudTelemetryBlock cacheBlock;
    private HudTelemetryBlock speedBlock;

    @SuppressWarnings("unused")
    private final Timer summaryClockTimer;

    public AiTabPanel(Font monoFont) {
        LlmSessionStatsTracker.getInstance(); // ensure tracker is registered before events flow
        sleeping = SystemSession.getInstance().isSleepingModeOn();
        EventBusManager.register(this);
        buildUi();
        summaryClockTimer = new Timer(1_000, e -> tickSummaryClock());
        summaryClockTimer.start();
    }

    public void dispose() {
        summaryClockTimer.stop();
        EventBusManager.unregister(this);
    }

    private static final int SIDEBAR_WIDTH = 220;

    private void buildUi() {
        setLayout(new BorderLayout(HUD_GAP, HUD_GAP));
        setBackground(HUD_BG);
        setBorder(hudDenseScreenBorder());

        // --- Controls wired up, placed in right sidebar SHORTCUTS ---
        startStopServicesButton = makeToggleButton(getText("button.startServices"));
        startStopServicesButton.addActionListener(e -> {
            EventBusManager.publish(new ToggleServicesEvent(!isServiceRunning.get()));
            startStopServicesButton.setEnabled(false);
        });

        toggleWakeWordOnOff = makeCheckBox(getText("ai.sleepWake"), false);
        toggleWakeWordOnOff.addActionListener(
                e -> EventBusManager.publish(new ToggleWakeWordEvent(toggleWakeWordOnOff.isSelected())));
        toggleWakeWordOnOff.setEnabled(false);
        toggleWakeWordOnOff.setForeground(ACCENT);

        final OBSOverlayWindow[] obsOverlay = {null};
        JCheckBox toggleObsOverlay = makeCheckBox(getText("ai.obsOverlay"), false);
        toggleObsOverlay.setForeground(ACCENT);
        toggleObsOverlay.setToolTipText(getText("ai.obsOverlay.tooltip"));
        toggleObsOverlay.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (toggleObsOverlay.isSelected()) {
                if (obsOverlay[0] == null) obsOverlay[0] = new OBSOverlayWindow();
                obsOverlay[0].setVisible(true);
            } else if (obsOverlay[0] != null) {
                obsOverlay[0].setVisible(false);
            }
        }));

        recalibrateAudioButton = makeButton(getText("button.calibrateAudio"));
        recalibrateAudioButton.setForeground(DISABLED_FG);
        recalibrateAudioButton.setEnabled(false);
        recalibrateAudioButton.addActionListener(e -> EventBusManager.publish(new RecalibrateAudioEvent()));

        // --- Log panels ---
        userPanel = new HudLogArea(30, HudLogArea.Style.USER_INPUT);

        aiPanel = new HudLogArea(25, HudLogArea.Style.AI_RESPONSE);

        systemPanel = new HudLogArea(12, HudLogArea.Style.SYSTEM_LOG);

        // --- Main log area (user/ai top, system below) ---
        HudSplitPane topSplit = new HudSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                logSection(getText("ai.section.userInput"), userPanel),
                logSection(getText("ai.section.aiResponse"), aiPanel)
        );
        topSplit.setResizeWeight(0.38);

        HudSplitPane mainSplit = new HudSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                topSplit,
                logSection(getText("ai.section.systemMessages"), systemPanel)
        );
        mainSplit.setResizeWeight(0.65);

        // --- Right sidebar ---
        JPanel sidebar = transparentPanel(new BorderLayout(0, HUD_GAP));
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));

        HudSection quickStatusSection = HudSection.compactCard(getText("ai.section.quickStatus"), new BorderLayout());
        quickStatusSection.body().add(buildQuickStatusPanel(), BorderLayout.NORTH);

        HudSection shortcutsSection = new HudSection(getText("ai.section.shortcuts"), new BorderLayout());
        shortcutsSection.body().add(buildShortcutsPanel(toggleObsOverlay), BorderLayout.NORTH);

        sidebar.add(quickStatusSection, BorderLayout.NORTH);
        sidebar.add(shortcutsSection, BorderLayout.CENTER);

        // --- Center: main logs + sidebar ---
        JPanel centerPanel = transparentPanel(new BorderLayout(HUD_GAP, 0));
        centerPanel.add(mainSplit, BorderLayout.CENTER);
        centerPanel.add(sidebar, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        // --- Bottom summary strip ---
        HudSection summarySection = HudSection.compactCard(getText("ai.section.systemSummary"), new BorderLayout());

        modelBlock       = new HudTelemetryBlock(getText("ai.summary.llmModel"),      tryLoadIcon("/images/microchip-ai.png"));
        sessionTimeBlock = new HudTelemetryBlock(getText("ai.summary.sessionTime"),   tryLoadIcon("/images/clock-five.png"));
        tokensBlock      = new HudTelemetryBlock(getText("ai.summary.tokensUsed"),    tryLoadIcon("/images/coins.png"));
        tphBlock         = new HudTelemetryBlock(getText("ai.summary.tokensPerHour"), tryLoadIcon("/images/tachometer-fast.png"));
        cacheBlock       = new HudTelemetryBlock(getText("ai.summary.cacheSaved"),    tryLoadIcon("/images/file-recycle.png"));
        speedBlock       = new HudTelemetryBlock(getText("ai.summary.lastSpeed"),     tryLoadIcon("/images/bolt.png"));

        HudTelemetryStrip strip = new HudTelemetryStrip();
        strip.addBlock(modelBlock);
        strip.addBlock(sessionTimeBlock);
        strip.addBlock(tokensBlock);
        strip.addBlock(tphBlock);
        strip.addBlock(cacheBlock);
        strip.addBlock(speedBlock);

        summarySection.body().add(strip, BorderLayout.CENTER);
        add(summarySection, BorderLayout.SOUTH);
    }

    /** Builds the vertical list of control buttons and checkboxes for the SHORTCUTS sidebar section. */
    private JPanel buildShortcutsPanel(JCheckBox toggleObsOverlay) {
        JPanel panel = transparentPanel(null);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        startStopServicesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        startStopServicesButton.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                startStopServicesButton.getPreferredSize().height));

        recalibrateAudioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        recalibrateAudioButton.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                recalibrateAudioButton.getPreferredSize().height));

        toggleWakeWordOnOff.setAlignmentX(Component.LEFT_ALIGNMENT);
        toggleObsOverlay.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(startStopServicesButton);
        panel.add(Box.createRigidArea(new Dimension(0, HUD_GAP)));
        panel.add(recalibrateAudioButton);
        panel.add(Box.createRigidArea(new Dimension(0, HUD_GAP)));
        panel.add(toggleWakeWordOnOff);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(toggleObsOverlay);

        return panel;
    }

    private HudSection logSection(String title, JComponent content) {
        HudSection section = new HudSection(title, new BorderLayout());
        section.body().add(content, BorderLayout.CENTER);
        return section;
    }

    public void initData(boolean streamingModeOn, boolean servicesRunning) {
        toggleWakeWordOnOff.setSelected(streamingModeOn);
        applyServiceState(servicesRunning);
    }

    public void addUserMessage(String text) {
        SwingUtilities.invokeLater(() -> userPanel.addMessage(text));
    }

    public void addAiMessage(String text) {
        SwingUtilities.invokeLater(() -> aiPanel.addMessage(text));
    }

    /** Renders a structured SYSTEM_LOG entry; timestamp is formatted as {@code HH:mm:ss}. */
    public void addSystemMessage(LocalTime timestamp, String text) {
        SwingUtilities.invokeLater(() -> systemPanel.addSystemLogEntry(timestamp, text));
    }

    @Subscribe
    public void onServiceStatusEvent(ServicesStateEvent event) {
        SwingUtilities.invokeLater(() -> {
            SleepNoThrow.sleep(1000);
            applyServiceState(event.isRunning());
        });
    }

    @Subscribe
    public void onStatsChanged(LlmSessionStatsChangedEvent event) {
        SwingUtilities.invokeLater(() -> refreshSummary(event.snapshot()));
    }

    private void applyServiceState(boolean running) {
        isServiceRunning.set(running);
        startStopServicesButton.setText(running ? getText("button.stopServices") : getText("button.startServices"));
        startStopServicesButton.setForeground(BUTTON_FG);
        startStopServicesButton.setBackground(BUTTON_BG);
        startStopServicesButton.setEnabled(true);
        recalibrateAudioButton.setEnabled(running);
        toggleWakeWordOnOff.setEnabled(running);
        refreshSttBadge();
        refreshLlmBadge();
        refreshTtsBadge();
    }

    /** Updates summary telemetry blocks from the latest stats snapshot. */
    private void refreshSummary(LlmSessionStatsSnapshot snap) {
        modelBlock.setValue(snap.lastModel());

        int totalTokens = snap.totalPromptTokens() + snap.totalCompletionTokens() + snap.totalCachedHits();
        tokensBlock.setValue(totalTokens > 0 ? fmtTokens(totalTokens) : null);

        int hits = snap.totalCachedHits();
        // Always show numeric value for cache (0 is informative, not "no data")
        cacheBlock.setValue(fmtTokens(hits));

        double tps = snap.lastTps();
        speedBlock.setValue(tps > 0 ? String.format("%.1f t/s", tps) : null);

        updateTph(snap);
    }

    /** Ticks the session-time and tokens-per-hour blocks every second. */
    private void tickSummaryClock() {
        LlmSessionStatsSnapshot snap = LlmSessionStatsTracker.getInstance().getSnapshot();
        Duration d = Duration.between(snap.sessionStart(), java.time.Instant.now());
        sessionTimeBlock.setValue(String.format("%02d:%02d:%02d",
                d.toHours(), d.toMinutesPart(), d.toSecondsPart()));
        updateTph(snap);
    }

    private void updateTph(LlmSessionStatsSnapshot snap) {
        // promptTokens = API input_tokens (excludes cache reads), so add all three buckets
        long elapsedSeconds = Duration.between(snap.sessionStart(), java.time.Instant.now()).toSeconds();
        if (elapsedSeconds < 600) {
            tphBlock.setValue(null); // collecting data
            return;
        }
        int total = snap.totalPromptTokens() + snap.totalCompletionTokens() + snap.totalCachedHits();
        if (total > 0) {
            long tph = Math.round(total / (elapsedSeconds / 3600.0));
            tphBlock.setValue(fmtTokens(tph) + "/hr");
        } else {
            tphBlock.setValue(null);
        }
    }

    private static String fmtTokens(long v) {
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000.0);
        if (v >= 1_000) return String.format("%.1fK", v / 1_000.0);
        return String.valueOf(v);
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

    // ── QUICK STATUS badge build and refresh ──────────────────────────────────

    /**
     * Builds the QUICK STATUS panel with live STT / LLM / TTS status rows.
     * Badges are initialised from current state and updated via event-driven refresh methods.
     */
    private JPanel buildQuickStatusPanel() {
        boolean running = isServiceRunning.get();

        String sttText = !running ? getText("hud.state.standby")
                : sleeping     ? getText("hud.state.sleeping")
                               : getText("hud.state.listening");
        StatusBadge.State sttState = (running && !sleeping) ? StatusBadge.State.OK : StatusBadge.State.STANDBY;
        sttBadge = new StatusBadge(sttText, sttState);

        llmBadge = new StatusBadge(
                running ? getText("hud.state.active") : getText("hud.state.standby"),
                running ? StatusBadge.State.OK : StatusBadge.State.STANDBY);

        String ttsText;
        StatusBadge.State ttsState;
        if (!running) {
            ttsText = getText("hud.state.standby");
            ttsState = StatusBadge.State.STANDBY;
        } else {
            boolean local = SystemSession.getInstance().useLocalTTS();
            ttsText = local ? getText("hud.tts.local") : getText("hud.tts.cloud");
            ttsState = StatusBadge.State.OK;
        }
        ttsBadge = new StatusBadge(ttsText, ttsState);

        int initCmdCount = CustomCommandRegistry.getInstance().getCustomCommands().size();
        bindingsBadge = new StatusBadge("—", StatusBadge.State.STANDBY);
        commandsBadge = new StatusBadge(getText("hud.commands.summary", initCmdCount), StatusBadge.State.INFO);
        keymapBadge   = new StatusBadge("—", StatusBadge.State.STANDBY);

        JPanel panel = transparentPanel(null);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(quickStatusRow(getText("hud.stt"), sttBadge));
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(quickStatusRow(getText("hud.llm"), llmBadge));
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(quickStatusRow(getText("hud.tts"), ttsBadge));
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(quickStatusRow(getText("hud.bindings"), bindingsBadge));
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(quickStatusRow(getText("hud.commands"), commandsBadge));
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(quickStatusRow(getText("hud.keymap"), keymapBadge));
        return panel;
    }

    /** Single key-label + badge row for the QUICK STATUS panel. */
    private static JPanel quickStatusRow(String key, StatusBadge badge) {
        JPanel row = transparentPanel(new BorderLayout(4, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, HUD_BADGE_HEIGHT + 4));
        JLabel label = new JLabel(key.toUpperCase() + ":");
        label.setForeground(FG_MUTED);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, HUD_FONT_XS));
        row.add(label, BorderLayout.WEST);
        row.add(badge, BorderLayout.EAST);
        return row;
    }

    private void refreshSttBadge() {
        boolean running = isServiceRunning.get();
        if (!running) {
            sttBadge.setStatus(getText("hud.state.standby"), StatusBadge.State.STANDBY);
        } else if (sleeping) {
            sttBadge.setStatus(getText("hud.state.sleeping"), StatusBadge.State.STANDBY);
        } else {
            sttBadge.setStatus(getText("hud.state.listening"), StatusBadge.State.OK);
        }
    }

    private void refreshLlmBadge() {
        boolean running = isServiceRunning.get();
        if (!running) {
            llmBadge.setStatus(getText("hud.state.standby"), StatusBadge.State.STANDBY);
        } else if (lastLlmProvider != null && !lastLlmProvider.isBlank()) {
            llmBadge.setStatus(lastLlmProvider, StatusBadge.State.OK);
        } else {
            llmBadge.setStatus(getText("hud.state.active"), StatusBadge.State.OK);
        }
    }

    private void refreshTtsBadge() {
        boolean running = isServiceRunning.get();
        if (!running) {
            ttsBadge.setStatus(getText("hud.state.standby"), StatusBadge.State.STANDBY);
        } else {
            boolean local = SystemSession.getInstance().useLocalTTS();
            ttsBadge.setStatus(
                    local ? getText("hud.tts.local") : getText("hud.tts.cloud"),
                    StatusBadge.State.OK);
        }
    }

    @Subscribe
    public void onBindingsSummaryChanged(BindingsSummaryChangedEvent event) {
        SwingUtilities.invokeLater(() -> {
            if (event.missing() > 0) {
                bindingsBadge.setStatus(getText("hud.bindings.badge.warn", event.missing()), StatusBadge.State.STANDBY);
            } else {
                bindingsBadge.setStatus(getText("hud.bindings.badge.ok"), StatusBadge.State.OK);
            }
        });
    }

    @Subscribe
    public void onCustomCommandsSummaryChanged(CustomCommandsSummaryChangedEvent event) {
        SwingUtilities.invokeLater(() ->
                commandsBadge.setStatus(getText("hud.commands.summary", event.count()), StatusBadge.State.INFO));
    }

    @Subscribe
    public void onKeymapSyncStateChanged(KeymapSyncStateChangedEvent event) {
        SwingUtilities.invokeLater(() -> {
            StatusBadge.State state = event.inSync() ? StatusBadge.State.OK : StatusBadge.State.STANDBY;
            String text = event.inSync() ? getText("hud.keymap.inSync") : getText("hud.keymap.modified");
            keymapBadge.setStatus(text, state);
        });
    }

    @Subscribe
    public void onVoiceInputModeToggle(VoiceInputModeToggleEvent event) {
        SwingUtilities.invokeLater(() -> toggleWakeWordOnOff.setSelected(event.isStreaming()));
    }

    @Subscribe
    public void onClearConsoleEvent(ClearConsoleEvent event) {
        SwingUtilities.invokeLater(() -> {
            userPanel.clear();
            aiPanel.clear();
            systemPanel.clear();
        });
    }

    /**
     * Loads a telemetry PNG via ImageIO into a decoded BufferedImage, then scales
     * to {@link HudTelemetryBlock#ICON_SIZE} with high-quality Graphics2D hints if the
     * source size differs. Returns {@code null} on failure so the block falls back to
     * its diamond marker. Result is constructed once and cached by the caller.
     */
    private static ImageIcon tryLoadIcon(String resource) {
        try {
            var url = AiTabPanel.class.getResource(resource);
            if (url == null) return null;
            BufferedImage src = ImageIO.read(url);
            if (src == null) return null;
            int target = HudTelemetryBlock.ICON_SIZE;
            if (src.getWidth() == target && src.getHeight() == target) {
                return new ImageIcon(src);
            }
            // High-quality one-time scale; not needed for current 32×32 PNGs but kept as a safety net.
            BufferedImage scaled = new BufferedImage(target, target, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(src, 0, 0, target, target, null);
            } finally {
                g2.dispose();
            }
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }
}

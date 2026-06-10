package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
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

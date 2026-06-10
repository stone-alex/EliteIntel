package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.*;
import elite.intel.util.SleepNoThrow;

import javax.swing.*;
import java.awt.*;
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

    public AiTabPanel(Font monoFont) {
        EventBusManager.register(this);
        buildUi();
    }

    public void dispose() {
        EventBusManager.unregister(this);
    }

    private static final int SIDEBAR_WIDTH = 220;

    private void buildUi() {
        setLayout(new BorderLayout(HUD_GAP, HUD_GAP));
        setBackground(HUD_BG);
        setBorder(hudScreenBorder());

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

    private void applyServiceState(boolean running) {
        isServiceRunning.set(running);
        startStopServicesButton.setText(running ? getText("button.stopServices") : getText("button.startServices"));
        startStopServicesButton.setForeground(BUTTON_FG);
        startStopServicesButton.setBackground(BUTTON_BG);
        startStopServicesButton.setEnabled(true);
        recalibrateAudioButton.setEnabled(running);
        toggleWakeWordOnOff.setEnabled(running);
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
}

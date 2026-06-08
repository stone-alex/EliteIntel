package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.*;
import elite.intel.util.SleepNoThrow;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;
import static elite.intel.ui.view.AppTheme.*;

public class AiTabPanel extends JPanel {

    JCheckBox toggleWakeWordOnOff;

    private JToggleButton startStopServicesButton;
    private JButton recalibrateAudioButton;
    private final AtomicBoolean isServiceRunning = new AtomicBoolean(false);

    private LogPanel userPanel;
    private LogPanel aiPanel;
    private LogPanel systemPanel;

    public AiTabPanel(Font monoFont) {
        EventBusManager.register(this);
        buildUi(monoFont);
    }

    public void dispose() {
        EventBusManager.unregister(this);
    }

    private void buildUi(Font monoFont) {
        setLayout(new BorderLayout(HUD_GAP, HUD_GAP));
        setBackground(HUD_BG);
        setBorder(BorderFactory.createEmptyBorder(HUD_PADDING, HUD_PADDING, HUD_PADDING, HUD_PADDING));

        JPanel buttons = transparentPanel(null);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        startStopServicesButton = makeToggleButton(getText("button.startServices"));
        startStopServicesButton.addActionListener(e -> {
            EventBusManager.publish(new ToggleServicesEvent(!isServiceRunning.get()));
            startStopServicesButton.setEnabled(false);
        });

        JCheckBox showDetailedLog = makeCheckBox(getText("ai.detailedLog"), false);
        showDetailedLog.addActionListener(
                e -> EventBusManager.publish(new ToggleDetailedLogEvent(showDetailedLog.isSelected())));
        showDetailedLog.setForeground(ACCENT);

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

        buttons.add(startStopServicesButton);
        buttons.add(Box.createRigidArea(new Dimension(8, 0)));
        buttons.add(recalibrateAudioButton);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(toggleWakeWordOnOff);
        buttons.add(Box.createRigidArea(new Dimension(8, 0)));
        buttons.add(toggleObsOverlay);
        buttons.add(Box.createRigidArea(new Dimension(8, 0)));
        //buttons.add(showDetailedLog);

        HudSection controlsSection = new HudSection(getText("ai.section.controls"), new BorderLayout());
        controlsSection.body().add(buttons, BorderLayout.CENTER);
        add(controlsSection, BorderLayout.NORTH);

        userPanel = new LogPanel(
                getText("ai.log.user"),
                new Color(0x252035), new Color(0xD4985A),
                new Color(0x1A1628), new Color(0xD4B087),
                monoFont, 30, false);

        aiPanel = new LogPanel(
                getText("ai.log.ai"),
                new Color(0x1A2520), ACCENT,
                new Color(0x141E18), new Color(0xA8D4B8),
                monoFont, 25, false);

        systemPanel = new LogPanel(
                getText("ai.log.system"),
                new Color(0x1A1E30), new Color(0x5A8AAA),
                new Color(0x161825), new Color(0x849AB4),
                monoFont, 12, true);

        JSplitPane topSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                logSection(getText("ai.section.userInput"), userPanel),
                logSection(getText("ai.section.aiResponse"), aiPanel)
        );
        topSplit.setResizeWeight(0.38);
        topSplit.setBackground(HUD_BG);
        topSplit.setBorder(null);
        topSplit.setDividerSize(HUD_GAP);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                topSplit,
                logSection(getText("ai.section.systemMessages"), systemPanel)
        );
        mainSplit.setResizeWeight(0.65);
        mainSplit.setBackground(HUD_BG);
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(HUD_GAP);

        add(mainSplit, BorderLayout.CENTER);
    }

    private HudSection logSection(String title, LogPanel logPanel) {
        HudSection section = new HudSection(title, new BorderLayout());
        section.body().add(logPanel, BorderLayout.CENTER);
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

    public void addSystemMessage(String text) {
        SwingUtilities.invokeLater(() -> systemPanel.addMessage(text));
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

package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.*;
import elite.intel.util.SleepNoThrow;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private void buildUi(Font monoFont) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        // Row 1: Button bar
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setOpaque(false);

        startStopServicesButton = makeToggleButton("Start Services");
        startStopServicesButton.addActionListener(e -> {
            EventBusManager.publish(new ToggleServicesEvent(!isServiceRunning.get()));
            startStopServicesButton.setEnabled(false);
        });

        JCheckBox showDetailedLog = new JCheckBox("Detailed Log", false);
        showDetailedLog.addActionListener(
                e -> EventBusManager.publish(new ToggleDetailedLogEvent(showDetailedLog.isSelected())));
        showDetailedLog.setForeground(ACCENT);

        toggleWakeWordOnOff = new JCheckBox("Listen to me / Ignore me", false);
        toggleWakeWordOnOff.addActionListener(
                e -> EventBusManager.publish(new ToggleWakeWordEvent(toggleWakeWordOnOff.isSelected())));
        toggleWakeWordOnOff.setEnabled(false);
        toggleWakeWordOnOff.setToolTipText(
                "Prevent AI from processing unless you prefix your command or query with word 'computer'");
        toggleWakeWordOnOff.setForeground(ACCENT);

        final OBSOverlayWindow[] obsOverlay = {null};
        JCheckBox toggleObsOverlay = new JCheckBox("OBS Overlay", false);
        toggleObsOverlay.setForeground(ACCENT);
        toggleObsOverlay.setToolTipText("Open a chroma-key green overlay window for OBS capture");
        toggleObsOverlay.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (toggleObsOverlay.isSelected()) {
                if (obsOverlay[0] == null) obsOverlay[0] = new OBSOverlayWindow();
                obsOverlay[0].setVisible(true);
            } else if (obsOverlay[0] != null) {
                obsOverlay[0].setVisible(false);
            }
        }));

        recalibrateAudioButton = makeButton("Calibrate Audio");
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
        buttons.add(showDetailedLog);

        add(new JLabel(" "));
        add(buttons, gbc);

        // Row 2: Three log panels in split panes
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        userPanel = new LogPanel(
                "USER",
                new Color(0x252035), new Color(0xD4985A),
                new Color(0x1A1628), new Color(0xD4B087),
                monoFont, 30, false);

        aiPanel = new LogPanel(
                "AI",
                new Color(0x1A2520), ACCENT,
                new Color(0x141E18), new Color(0xA8D4B8),
                monoFont, 25, false);

        systemPanel = new LogPanel(
                "SYSTEM",
                new Color(0x1A1E30), new Color(0x5A8AAA),
                new Color(0x161825), new Color(0x849AB4),
                monoFont, 12, true);

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userPanel, aiPanel);
        topSplit.setResizeWeight(0.38);
        topSplit.setBackground(BG);
        topSplit.setBorder(null);
        topSplit.setDividerSize(4);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, systemPanel);
        mainSplit.setResizeWeight(0.65);
        mainSplit.setBackground(BG);
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(4);

        add(mainSplit, gbc);
    }

    public void initData(boolean streamingModeOn) {
        toggleWakeWordOnOff.setSelected(streamingModeOn);
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
            isServiceRunning.set(event.isRunning());
            startStopServicesButton.setText(event.isRunning() ? "Stop Services" : "Start Services");
            startStopServicesButton.setForeground(BUTTON_FG);
            startStopServicesButton.setBackground(BUTTON_BG);
            startStopServicesButton.setEnabled(true);
            recalibrateAudioButton.setEnabled(event.isRunning());
            toggleWakeWordOnOff.setEnabled(event.isRunning());
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
}

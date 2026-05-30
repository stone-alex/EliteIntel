package elite.intel.ui.view.settings;

import elite.intel.db.managers.ShipManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.NotificationVolumeChangedEvent;
import elite.intel.ui.event.SpeechSpeedChangeEvent;
import elite.intel.ui.event.SttThreadsChangedEvent;
import elite.intel.ui.event.SttVolumeChangedEvent;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static elite.intel.ui.view.AppTheme.BUTTON_BG;
import static elite.intel.ui.view.AppTheme.baseGbc;
import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

public class AudioSettingsPanel extends JPanel {

    private final SystemSession systemSession = SystemSession.getInstance();

    private JSlider voiceVolumeSlider;
    private JSlider beepVolumeSlider;
    private JSlider speechSpeedSlider;
    private JSlider sttThreadsSlider;
    private JCheckBox useLocalTTSCheck;

    private Runnable onLocalTtsChanged;

    public void setOnLocalTtsChanged(Runnable r) {
        onLocalTtsChanged = r;
    }

    public AudioSettingsPanel() {
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());

        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints ag = baseGbc();

        // Row 0: Speech Volume | Beep Volume
        ag.gridy = 0;
        ag.gridx = 0;
        ag.weightx = 0;
        ag.fill = GridBagConstraints.NONE;
        JLabel lblSpeechVol = new JLabel(getText("settings.audio.speechVolume"));
        lblSpeechVol.setPreferredSize(new Dimension(140, 42));
        grid.add(lblSpeechVol, ag);

        voiceVolumeSlider = makeSlider(0, 100, systemSession.getVoiceVolume(), 25, 1);
        voiceVolumeSlider.addChangeListener(e -> EventBusManager.publish(new SttVolumeChangedEvent(voiceVolumeSlider.getValue())));
        ag.gridx = 1;
        ag.weightx = 1.0;
        ag.fill = GridBagConstraints.HORIZONTAL;
        grid.add(voiceVolumeSlider, ag);

        ag.gridx = 2;
        ag.weightx = 0;
        ag.fill = GridBagConstraints.NONE;
        ag.insets = new Insets(6, 24, 6, 6);
        JLabel lblBeepVol = new JLabel(getText("settings.audio.beepVolume"));
        lblBeepVol.setPreferredSize(new Dimension(120, 42));
        grid.add(lblBeepVol, ag);

        beepVolumeSlider = makeSlider(0, 100, (int) (systemSession.getBeepVolume() * 100), 25, 1);
        beepVolumeSlider.addChangeListener(e -> EventBusManager.publish(new NotificationVolumeChangedEvent(beepVolumeSlider.getValue() / 100f)));
        ag.gridx = 3;
        ag.weightx = 1.0;
        ag.fill = GridBagConstraints.HORIZONTAL;
        ag.insets = new Insets(6, 6, 6, 6);
        grid.add(beepVolumeSlider, ag);

        // Row 1: TTS Voice Speed | STT Threads
        ag.gridy = 1;
        ag.gridx = 0;
        ag.weightx = 0;
        ag.fill = GridBagConstraints.NONE;
        JLabel lblTtsSpeed = new JLabel(getText("settings.audio.ttsVoiceSpeed"));
        lblTtsSpeed.setPreferredSize(new Dimension(140, 42));
        grid.add(lblTtsSpeed, ag);

        speechSpeedSlider = makeSlider(0, 100, (int) (systemSession.getSpeechSpeed() * 100), 25, 1);
        speechSpeedSlider.addChangeListener(e -> EventBusManager.publish(new SpeechSpeedChangeEvent(speechSpeedSlider.getValue() / 100f)));
        ag.gridx = 1;
        ag.weightx = 1.0;
        ag.fill = GridBagConstraints.HORIZONTAL;
        grid.add(speechSpeedSlider, ag);

        ag.gridx = 2;
        ag.weightx = 0;
        ag.fill = GridBagConstraints.NONE;
        ag.insets = new Insets(6, 24, 6, 6);
        JLabel lblSttThreads = new JLabel(getText("settings.audio.sttThreads"));
        lblSttThreads.setPreferredSize(new Dimension(120, 42));
        grid.add(lblSttThreads, ag);

        sttThreadsSlider = makeSlider(4, 11, systemSession.getSttThreads(), 1, 1);
        sttThreadsSlider.addChangeListener(e -> EventBusManager.publish(new SttThreadsChangedEvent(sttThreadsSlider.getValue())));
        ag.gridx = 3;
        ag.weightx = 1.0;
        ag.fill = GridBagConstraints.HORIZONTAL;
        ag.insets = new Insets(6, 6, 6, 6);
        grid.add(sttThreadsSlider, ag);

        // Row 2: Use Local TTS
        ag.gridy = 2;
        ag.gridx = 0;
        ag.gridwidth = 4;
        ag.weightx = 0;
        ag.fill = GridBagConstraints.NONE;
        useLocalTTSCheck = new JCheckBox(getText("settings.audio.useLocalTts"), false);
        useLocalTTSCheck.addActionListener(a -> saveLocalTts());
        grid.add(useLocalTTSCheck, ag);

        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BUTTON_BG, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(grid);

        // -- Mic monitor ------------------------------------------------------
        content.add(Box.createVerticalStrut(8));
        AudioWaveformPanel waveformPanel = new AudioWaveformPanel();
        waveformPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        waveformPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, AudioWaveformPanel.PANEL_HEIGHT));
        content.add(waveformPanel);

        JPanel helpLabels = new JPanel();
        helpLabels.setLayout(new BoxLayout(helpLabels, BoxLayout.PAGE_AXIS));
        helpLabels.setOpaque(false);
        helpLabels.setAlignmentX(Component.LEFT_ALIGNMENT);
        helpLabels.add(Box.createVerticalStrut(18));
        JLabel graphLabel = new JLabel(getText("settings.audio.micHelp"));
        helpLabels.add(graphLabel);
        content.add(helpLabels);

        add(content, BorderLayout.NORTH);
    }

    public void initData() {
        useLocalTTSCheck.setSelected(systemSession.useLocalTTS());
    }

    /**
     * Called by CloudServicesSettingsPanel when the user activates cloud TTS.
     * Delegates to saveLocalTts() so the confirmation dialog and voice-reset
     * logic fire identically to the user clicking the checkbox directly.
     */
    public void activateCloudTts() {
        useLocalTTSCheck.setSelected(false);
        saveLocalTts();
    }

    private void saveLocalTts() {
        boolean newValue = useLocalTTSCheck.isSelected();
        boolean oldValue = systemSession.useLocalTTS();
        if (newValue != oldValue) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    getText("settings.audio.switchTts.message"),
                    getText("settings.audio.switchTts.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                useLocalTTSCheck.setSelected(oldValue);
                return;
            }
            ShipManager.getInstance().resetAllVoicesToDefault();
        }
        systemSession.setUseLocalTTS(newValue);
        if (onLocalTtsChanged != null) onLocalTtsChanged.run();
    }

    private static JSlider makeSlider(int min, int max, int value, int majorTick, int minorTick) {
        JSlider s = new JSlider(min, max, value);
        s.setMajorTickSpacing(majorTick);
        s.setMinorTickSpacing(minorTick);
        s.setSnapToTicks(true);
        s.setPaintTicks(false);
        s.setPaintLabels(true);
        return s;
    }
}

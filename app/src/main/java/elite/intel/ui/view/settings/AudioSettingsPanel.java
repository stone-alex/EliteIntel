package elite.intel.ui.view.settings;

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

public class AudioSettingsPanel extends JPanel {

    private final SystemSession systemSession = SystemSession.getInstance();

    private JSlider voiceVolumeSlider;
    private JSlider beepVolumeSlider;
    private JSlider speechSpeedSlider;
    private JSlider whisperThreadsSlider;
    private JCheckBox useLocalTTSCheck;

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
        JLabel lblSpeechVol = new JLabel("Speech Volume");
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
        JLabel lblBeepVol = new JLabel("Beep Volume");
        lblBeepVol.setPreferredSize(new Dimension(120, 42));
        grid.add(lblBeepVol, ag);

        beepVolumeSlider = makeSlider(0, 100, (int) (systemSession.getBeepVolume() * 100), 25, 1);
        beepVolumeSlider.addChangeListener(e -> EventBusManager.publish(new NotificationVolumeChangedEvent(beepVolumeSlider.getValue() / 100f)));
        ag.gridx = 3;
        ag.weightx = 1.0;
        ag.fill = GridBagConstraints.HORIZONTAL;
        grid.add(beepVolumeSlider, ag);

        // Row 1: TTS Voice Speed | STT Threads
        ag.gridy = 1;
        ag.gridx = 0;
        ag.weightx = 0;
        ag.fill = GridBagConstraints.NONE;
        JLabel lblTtsSpeed = new JLabel("TTS Voice Speed");
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
        JLabel lblSttThreads = new JLabel("STT Threads");
        lblSttThreads.setPreferredSize(new Dimension(120, 42));
        grid.add(lblSttThreads, ag);

        whisperThreadsSlider = makeSlider(4, 10, systemSession.getSttThreads(), 1, 1);
        whisperThreadsSlider.addChangeListener(e -> EventBusManager.publish(new SttThreadsChangedEvent(whisperThreadsSlider.getValue())));
        ag.gridx = 3;
        ag.weightx = 1.0;
        ag.fill = GridBagConstraints.HORIZONTAL;
        grid.add(whisperThreadsSlider, ag);

        // Row 2: Use Local TTS
        ag.gridy = 2;
        ag.gridx = 0;
        ag.gridwidth = 4;
        ag.weightx = 0;
        ag.fill = GridBagConstraints.NONE;
        useLocalTTSCheck = new JCheckBox("Use Local Text To Speech", false);
        useLocalTTSCheck.addActionListener(a -> saveLocalTts());
        grid.add(useLocalTTSCheck, ag);

        grid.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BUTTON_BG, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(grid);

        add(content, BorderLayout.NORTH);
    }

    public void initData() {
        useLocalTTSCheck.setSelected(systemSession.useLocalTTS());
    }

    private void saveLocalTts() {
        systemSession.setUseLocalTTS(useLocalTTSCheck.isSelected());
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

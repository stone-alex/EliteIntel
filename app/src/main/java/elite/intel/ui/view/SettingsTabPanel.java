package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.*;
import elite.intel.util.Updater;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static elite.intel.ui.view.AppTheme.*;

public class SettingsTabPanel extends JPanel {

    private final SystemSession systemSession = SystemSession.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();

    private JPasswordField llmApiKeyField;
    private JPasswordField ttsApiKeyField;
    private JCheckBox llmLockedCheck;
    private JCheckBox ttsLockedCheck;
    private JTextField localLlmAddressField;
    private JTextField localLlmModelCommandField;
    private JTextField localLlmModelQueryField;
    private JCheckBox useLocalCommandLLMCheck;
    private JCheckBox useLocalQueryLLMCheck;
    private JCheckBox useLocalTTSCheck;
    private JSlider speechSpeedSlider;
    private JSlider beepVolumeSlider;
    private JButton updateAppButton;

    public SettingsTabPanel() {
        EventBusManager.register(this);
        buildUi();
    }

    private void buildUi() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        GridBagConstraints gbc = baseGbc();

        // ── Cloud Settings ────────────────────────────────────────────────────
        nextRow(gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel cloudFields = new JPanel(new GridBagLayout());
        addLabel(cloudFields, "Cloud LLM Key: (optional)", gbc);
        llmApiKeyField = new JPasswordField();
        llmApiKeyField.setPreferredSize(new Dimension(200, 42));
        addField(cloudFields, llmApiKeyField, gbc, 1, 0.8);
        llmLockedCheck = new JCheckBox("Locked", true);
        addCheck(cloudFields, llmLockedCheck, gbc);

        nextRow(gbc);
        addLabel(cloudFields, "Google TTS Key: (optional)", gbc);
        ttsApiKeyField = new JPasswordField();
        ttsApiKeyField.setPreferredSize(new Dimension(200, 42));
        addField(cloudFields, ttsApiKeyField, gbc, 1, 0.8);
        ttsLockedCheck = new JCheckBox("Locked", true);
        addCheck(cloudFields, ttsLockedCheck, gbc);
        cloudFields.setBorder(new LineBorder(ACCENT, 1));
        addNestedPanel(this, cloudFields, "CLOUD SETTINGS");

        // ── Local Settings ────────────────────────────────────────────────────
        JPanel localSettingsPanel = new JPanel(new GridBagLayout());
        nextRow(gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nextRow(gbc);
        addLabel(localSettingsPanel, "Local LLM Address:", gbc);
        localLlmAddressField = new JTextField();
        localLlmAddressField.setPreferredSize(new Dimension(200, 42));
        addField(localSettingsPanel, localLlmAddressField, gbc, 1, 0.8);

        nextRow(gbc);
        addLabel(localSettingsPanel, "Local Command LLM:", gbc);
        localLlmModelCommandField = new JTextField();
        localLlmModelCommandField.setPreferredSize(new Dimension(200, 42));
        addField(localSettingsPanel, localLlmModelCommandField, gbc, 1, 0.8);
        useLocalCommandLLMCheck = new JCheckBox("Use", false);
        useLocalCommandLLMCheck.addActionListener(a -> SwingUtilities.invokeLater(this::saveSystemConfig));
        addCheck(localSettingsPanel, useLocalCommandLLMCheck, gbc);

        nextRow(gbc);
        addLabel(localSettingsPanel, "Local Query LLM:", gbc);
        localLlmModelQueryField = new JTextField();
        localLlmModelQueryField.setPreferredSize(new Dimension(200, 42));
        useLocalQueryLLMCheck = new JCheckBox("Use", false);
        useLocalQueryLLMCheck.addActionListener(a -> SwingUtilities.invokeLater(this::saveSystemConfig));
        addField(localSettingsPanel, localLlmModelQueryField, gbc, 1, 0.8);
        addCheck(localSettingsPanel, useLocalQueryLLMCheck, gbc);

        nextRow(gbc);
        addLabel(localSettingsPanel, "Local TTS", gbc);
        useLocalTTSCheck = new JCheckBox("Use", false);
        useLocalTTSCheck.addActionListener(a -> SwingUtilities.invokeLater(this::saveSystemConfig));
        addCheck(localSettingsPanel, useLocalTTSCheck, gbc);

        nextRow(gbc);
        nextRow(gbc);
        addLabel(localSettingsPanel, "Speech Speed ", gbc);
        speechSpeedSlider = new JSlider(0, 100, (int) (systemSession.getSpeechSpeed() * 100));
        speechSpeedSlider.setMajorTickSpacing(25);
        speechSpeedSlider.setMinorTickSpacing(1);
        speechSpeedSlider.setSnapToTicks(true);
        speechSpeedSlider.setPaintTicks(true);
        speechSpeedSlider.setPaintLabels(true);
        addField(localSettingsPanel, speechSpeedSlider, gbc, 1, 0.8);
        speechSpeedSlider.addChangeListener(e -> EventBusManager.publish(
                new SpeechSpeedChangeEvent(Math.abs(speechSpeedSlider.getValue()) / 100f)));

        nextRow(gbc);
        addLabel(localSettingsPanel, "Beep Volume ", gbc);
        beepVolumeSlider = new JSlider(0, 100, (int) (systemSession.getBeepVolume() * 100));
        beepVolumeSlider.setMajorTickSpacing(25);
        beepVolumeSlider.setMinorTickSpacing(1);
        beepVolumeSlider.setSnapToTicks(true);
        beepVolumeSlider.setPaintTicks(true);
        beepVolumeSlider.setPaintLabels(true);
        addField(localSettingsPanel, beepVolumeSlider, gbc, 1, 0.8);
        beepVolumeSlider.addChangeListener(e -> EventBusManager.publish(
                new NotificationVolumeChangedEvent(Math.abs(beepVolumeSlider.getValue()) / 100f)));

        localSettingsPanel.setBorder(new LineBorder(ACCENT, 1));
        addNestedPanel(this, localSettingsPanel, "OFF LINE SETTINGS");
        addNestedPanel(this, localSettingsPanel, "");

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setOpaque(false);

        JButton saveSystemButton = makeButton("Save Configuration");
        saveSystemButton.addActionListener(e -> saveSystemConfig());

        updateAppButton = makeButtonSubtle("App is Up to Date");
        updateAppButton.setEnabled(false);
        updateAppButton.addActionListener(e -> {
            updateAppButton.setEnabled(false);
            updateAppButton.setText("Updating…");
            Updater.performUpdateAsync().thenAccept(launched -> {
                if (launched) {
                    EventBusManager.publish(new SystemShutDownEvent());
                } else {
                    SwingUtilities.invokeLater(() -> {
                        updateAppButton.setEnabled(true);
                        updateAppButton.setText("Update Available");
                    });
                    EventBusManager.publish(new AppLogEvent(
                            "Could not launch updater - is elite_intel_updater.jar present?"));
                }
            });
        });

        JButton restoreDefaultsButton = makeButton("Restore Local LLM Defaults");
        restoreDefaultsButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            localLlmAddressField.setText("http://localhost:11434/api/chat");
            localLlmModelCommandField.setText("tulu3:8b");
            localLlmModelQueryField.setText("tulu3:8b");
            saveSystemConfig();
        }));

        buttons.add(saveSystemButton);
        buttons.add(updateAppButton);
        buttons.add(restoreDefaultsButton);

        add(new JLabel(" "));
        add(buttons);

        bindLock(llmLockedCheck, llmApiKeyField);
        bindLock(ttsLockedCheck, ttsApiKeyField);
    }

    public void initData() {
        llmApiKeyField.setText(systemSession.getAiApiKey() != null ? systemSession.getAiApiKey() : "");
        ttsApiKeyField.setText(systemSession.getTtsApiKey() != null ? systemSession.getTtsApiKey() : "");
        localLlmAddressField.setText(playerSession.getLocalLlmAddress() != null ? playerSession.getLocalLlmAddress() : "");
        localLlmModelCommandField.setText(systemSession.getLocalLlmCommandModel() != null ? systemSession.getLocalLlmCommandModel() : "");
        localLlmModelQueryField.setText(systemSession.getLocalLlmQueryModel() != null ? systemSession.getLocalLlmQueryModel() : "");
        useLocalCommandLLMCheck.setSelected(systemSession.useLocalCommandLlm());
        useLocalQueryLLMCheck.setSelected(systemSession.useLocalQueryLlm());
        useLocalTTSCheck.setSelected(systemSession.useLocalTTS());
    }

    private void saveSystemConfig() {
        systemSession.setAiApiKey(new String(llmApiKeyField.getPassword()));
        systemSession.setTtsApiKey(new String(ttsApiKeyField.getPassword()));
        playerSession.setLocalLlmAddress(localLlmAddressField.getText());
        systemSession.setLocalLlmCommandModel(localLlmModelCommandField.getText());
        systemSession.setLocalLlmQueryModel(localLlmModelQueryField.getText());
        systemSession.setUseLocalCommandLlm(useLocalCommandLLMCheck.isSelected());
        systemSession.setUseLocalQueryLlm(useLocalQueryLLMCheck.isSelected());
        systemSession.setUseLocalTTS(useLocalTTSCheck.isSelected());
        EventBusManager.publish(new AppLogEvent("System config saved"));
        initData();
    }

    @Subscribe
    public void onUpdateAvailableEvent(UpdateAvailableEvent event) {
        SwingUtilities.invokeLater(() -> {
            updateAppButton.setEnabled(true);
            updateAppButton.setText("Update Available");
        });
    }
}

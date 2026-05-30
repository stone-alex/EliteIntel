package elite.intel.ui.view.settings;

import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.ui.event.RestartBrainEvent;
import elite.intel.ui.event.RestartMouthEvent;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Objects;

import static elite.intel.ui.view.AppTheme.*;
import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

public class CloudServicesSettingsPanel extends JPanel {

    private final SystemSession systemSession = SystemSession.getInstance();

    private JPasswordField llmApiKeyField;
    private JPasswordField ttsApiKeyField;
    private JCheckBox llmLockedCheck;
    private JCheckBox ttsLockedCheck;
    private JCheckBox useCloudLlmCheck;
    private JCheckBox useCloudTtsCheck;

    /**
     * Called when the user activates cloud LLM - wired in by SettingsTabPanel.
     */
    private Runnable onCloudLlmUsed;
    /**
     * Called when the user activates cloud TTS - wired in by SettingsTabPanel.
     */
    private Runnable onCloudTtsUsed;

    public void setOnCloudLlmUsed(Runnable r) {
        onCloudLlmUsed = r;
    }

    public void setOnCloudTtsUsed(Runnable r) {
        onCloudTtsUsed = r;
    }

    public CloudServicesSettingsPanel() {
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gc = baseGbc();

        addLabel(fields, getText("settings.cloud.llmKey"), gc);
        llmApiKeyField = new JPasswordField();
        llmApiKeyField.setPreferredSize(new Dimension(200, 42));
        addField(fields, llmApiKeyField, gc, 1, 0.8);
        llmLockedCheck = new JCheckBox(getText("settings.cloud.locked"), true);
        addCheck(fields, llmLockedCheck, gc);
        useCloudLlmCheck = new JCheckBox(getText("settings.cloud.use"), false);
        useCloudLlmCheck.addActionListener(e -> onUseCloudLlm());
        gc.gridx = 3;
        gc.weightx = 0.2;
        gc.fill = GridBagConstraints.NONE;
        fields.add(useCloudLlmCheck, gc);

        nextRow(gc);
        addLabel(fields, getText("settings.cloud.ttsKey"), gc);
        ttsApiKeyField = new JPasswordField();
        ttsApiKeyField.setPreferredSize(new Dimension(200, 42));
        addField(fields, ttsApiKeyField, gc, 1, 0.8);
        ttsLockedCheck = new JCheckBox(getText("settings.cloud.locked"), true);
        addCheck(fields, ttsLockedCheck, gc);
        useCloudTtsCheck = new JCheckBox(getText("settings.cloud.use"), false);
        useCloudTtsCheck.addActionListener(e -> onUseCloudTts());
        gc.gridx = 3;
        gc.weightx = 0.2;
        gc.fill = GridBagConstraints.NONE;
        fields.add(useCloudTtsCheck, gc);

        fields.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BUTTON_BG, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);

        JButton saveButton = makeButton(getText("button.save"));
        saveButton.addActionListener(e -> save());
        buttons.add(saveButton);

        JPanel supportedLLMsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        supportedLLMsPanel.setOpaque(false);

        buttons.add(Box.createVerticalGlue());
        buttons.add(new JLabel(" " + getText("settings.cloud.supportedLlms")));
        buttons.add(new JLabel(" " + getText("settings.cloud.supportedLlms.names")));
        buttons.add(new JLabel(" " + getText("settings.cloud.modelAutoSelected")));


        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(fields);
        content.add(Box.createVerticalStrut(12));
        content.add(buttons);


        add(content, BorderLayout.NORTH);

        bindLock(llmLockedCheck, llmApiKeyField);
        bindLock(ttsLockedCheck, ttsApiKeyField);
    }

    public void initData() {
        llmApiKeyField.setText(systemSession.getAiApiKey() != null ? systemSession.getAiApiKey() : "");
        ttsApiKeyField.setText(systemSession.getTtsApiKey() != null ? systemSession.getTtsApiKey() : "");
        // Derive "Use" state: cloud is active when neither local option is selected
        useCloudLlmCheck.setSelected(!systemSession.useLocalCommandLlm() && !systemSession.useLocalQueryLlm());
        useCloudTtsCheck.setSelected(!systemSession.useLocalTTS());
    }

    /**
     * Re-derives the two "Use" checkbox states from SystemSession.
     * Called by the local panels whenever their own Use state changes.
     */
    public void syncUseCheckboxes() {
        useCloudLlmCheck.setSelected(!systemSession.useLocalCommandLlm() && !systemSession.useLocalQueryLlm());
        useCloudTtsCheck.setSelected(!systemSession.useLocalTTS());
    }

    private void onUseCloudLlm() {
        if (useCloudLlmCheck.isSelected() && onCloudLlmUsed != null) {
            onCloudLlmUsed.run();
        }
        // Re-sync from session in case the action changed nothing or was a no-op
        useCloudLlmCheck.setSelected(!systemSession.useLocalCommandLlm() && !systemSession.useLocalQueryLlm());
    }

    private void onUseCloudTts() {
        if (useCloudTtsCheck.isSelected() && onCloudTtsUsed != null) {
            onCloudTtsUsed.run();
        }
        // Re-sync from session - if the TTS confirmation dialog was cancelled,
        // systemSession.useLocalTTS() is still true so this reverts the checkbox
        useCloudTtsCheck.setSelected(!systemSession.useLocalTTS());
    }

    private void save() {
        String oldAiKey = systemSession.getAiApiKey();
        String oldTtsKey = systemSession.getTtsApiKey();
        boolean useLocalTts = systemSession.useLocalTTS();
        String newAiKey = new String(llmApiKeyField.getPassword());
        String newTtsKey = new String(ttsApiKeyField.getPassword());

        systemSession.setAiApiKey(newAiKey);
        systemSession.setTtsApiKey(newTtsKey);
        EventBusManager.publish(new AppLogEvent("Cloud services config saved"));
        if (!Objects.equals(oldAiKey, newAiKey)) EventBusManager.publish(new RestartBrainEvent());
        if (!useLocalTts && !Objects.equals(oldTtsKey, newTtsKey)) EventBusManager.publish(new RestartMouthEvent());
        initData();
    }
}

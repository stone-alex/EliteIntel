package elite.intel.ui.view.settings;

import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.ui.event.RestartBrainEvent;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static elite.intel.ui.view.AppTheme.*;

public class LocalLlmSettingsPanel extends JPanel {

    private final SystemSession systemSession = SystemSession.getInstance();
    private final PlayerSession playerSession = PlayerSession.getInstance();

    private JTextField localLlmAddressField;
    private JTextField localLlmModelCommandField;
    private JTextField localLlmModelQueryField;
    private JCheckBox useLocalCommandLLMCheck;
    private JCheckBox useLocalQueryLLMCheck;

    public LocalLlmSettingsPanel() {
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gc = baseGbc();

        addLabel(fields, "Local LLM Address:", gc);
        localLlmAddressField = new JTextField();
        addField(fields, localLlmAddressField, gc, 1, 1.0);

        nextRow(gc);
        addLabel(fields, "Local Command LLM:", gc);
        localLlmModelCommandField = new JTextField();
        addField(fields, localLlmModelCommandField, gc, 1, 1.0);
        useLocalCommandLLMCheck = new JCheckBox("Use", false);
        useLocalCommandLLMCheck.addActionListener(e -> onCheckboxToggled());
        addCheck(fields, useLocalCommandLLMCheck, gc);

        nextRow(gc);
        addLabel(fields, "Local Query LLM:", gc);
        localLlmModelQueryField = new JTextField();
        addField(fields, localLlmModelQueryField, gc, 1, 1.0);
        useLocalQueryLLMCheck = new JCheckBox("Use", false);
        useLocalQueryLLMCheck.addActionListener(e -> onCheckboxToggled());
        addCheck(fields, useLocalQueryLLMCheck, gc);

        fields.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BUTTON_BG, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);

        JButton saveButton = makeButton("Save");
        saveButton.addActionListener(e -> save());

        JButton restoreButton = makeButton("Restore Defaults");
        restoreButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            localLlmAddressField.setText("http://localhost:11434/api/chat");
            localLlmModelCommandField.setText("tulu3:8b");
            localLlmModelQueryField.setText("tulu3:8b");
            save();
        }));

        buttons.add(saveButton);
        buttons.add(restoreButton);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(fields);
        content.add(Box.createVerticalStrut(12));
        content.add(buttons);

        add(content, BorderLayout.NORTH);
    }

    public void initData() {
        localLlmAddressField.setText(playerSession.getLocalLlmAddress() != null ? playerSession.getLocalLlmAddress() : "");
        localLlmModelCommandField.setText(systemSession.getLocalLlmCommandModel() != null ? systemSession.getLocalLlmCommandModel() : "");
        localLlmModelQueryField.setText(systemSession.getLocalLlmQueryModel() != null ? systemSession.getLocalLlmQueryModel() : "");
        useLocalCommandLLMCheck.setSelected(systemSession.useLocalCommandLlm());
        useLocalQueryLLMCheck.setSelected(systemSession.useLocalQueryLlm());
    }

    private void onCheckboxToggled() {
        systemSession.setUseLocalCommandLlm(useLocalCommandLLMCheck.isSelected());
        systemSession.setUseLocalQueryLlm(useLocalQueryLLMCheck.isSelected());
        EventBusManager.publish(new AppLogEvent("LLM mode changed: command="
                + (useLocalCommandLLMCheck.isSelected() ? "local" : "cloud")
                + " query=" + (useLocalQueryLLMCheck.isSelected() ? "local" : "cloud")));
        EventBusManager.publish(new RestartBrainEvent());
    }

    private void save() {
        playerSession.setLocalLlmAddress(localLlmAddressField.getText());
        systemSession.setLocalLlmCommandModel(localLlmModelCommandField.getText());
        systemSession.setLocalLlmQueryModel(localLlmModelQueryField.getText());
        systemSession.setUseLocalCommandLlm(useLocalCommandLLMCheck.isSelected());
        systemSession.setUseLocalQueryLlm(useLocalQueryLLMCheck.isSelected());
        EventBusManager.publish(new AppLogEvent("Local LLM config saved"));
        EventBusManager.publish(new RestartBrainEvent());
        initData();
    }
}

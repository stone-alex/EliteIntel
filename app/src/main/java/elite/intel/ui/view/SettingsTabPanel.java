package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.ui.event.SystemShutDownEvent;
import elite.intel.ui.event.UpdateAvailableEvent;
import elite.intel.ui.view.settings.AudioSettingsPanel;
import elite.intel.ui.view.settings.CloudServicesSettingsPanel;
import elite.intel.ui.view.settings.LocalLlmSettingsPanel;
import elite.intel.util.Updater;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static elite.intel.ui.view.AppTheme.makeButtonSubtle;
import static elite.intel.ui.view.AppTheme.styleTabbedPane;

public class SettingsTabPanel extends JPanel {

    private final LocalLlmSettingsPanel localLlmPanel = new LocalLlmSettingsPanel();
    private final AudioSettingsPanel audioPanel = new AudioSettingsPanel();
    private final CloudServicesSettingsPanel cloudPanel = new CloudServicesSettingsPanel();

    private JButton updateAppButton;

    public SettingsTabPanel() {
        EventBusManager.register(this);
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.TOP);
        styleTabbedPane(tabs);
        tabs.addTab("Local LLM", scaledIcon("/images/local-llm.png"), localLlmPanel);
        tabs.addTab("Audio", scaledIcon("/images/audio.png"), audioPanel);
        tabs.addTab("Cloud Services", scaledIcon("/images/cloud.png"), cloudPanel);

        updateAppButton = makeButtonSubtle("App is Up to Date");
        updateAppButton.setEnabled(false);
        updateAppButton.setIcon(scaledIcon("/images/update.png"));
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

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        footer.setOpaque(false);
        footer.add(updateAppButton);

        add(tabs, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    public void initData() {
        localLlmPanel.initData();
        audioPanel.initData();
        cloudPanel.initData();
    }

    @Subscribe
    public void onUpdateAvailableEvent(UpdateAvailableEvent event) {
        SwingUtilities.invokeLater(() -> {
            updateAppButton.setEnabled(true);
            updateAppButton.setText("Update Available");
        });
    }

    private ImageIcon scaledIcon(String resource) {
        return new ImageIcon(
                new ImageIcon(Objects.requireNonNull(getClass().getResource(resource)))
                        .getImage().getScaledInstance(42, 42, Image.SCALE_SMOOTH));
    }
}

package elite.intel.ui.view;

import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.AppLogEvent;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static elite.intel.ui.view.AppTheme.*;

public class PlayerTabPanel extends JPanel {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    private JTextField playerAltNameField;
    private JTextField playerTitleField;
    private JTextField playerMissionDescription;
    private JTextField journalDirField;
    private JTextField bindingsDirField;

    public PlayerTabPanel() {
        buildUi();
    }

    private void buildUi() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();

        // Row 0: Commander Name
        addLabel(this, "Commander Name:", gbc);
        playerAltNameField = new JTextField();
        playerAltNameField.setToolTipText("If you want Elite Intel refer to you by name once in a while.");
        playerAltNameField.setPreferredSize(new Dimension(200, 42));
        addField(this, playerAltNameField, gbc, 1, 1.0);

        // Row 1: Title
        nextRow(gbc);
        addLabel(this, "Title:", gbc);
        playerTitleField = new JTextField();
        playerTitleField.setToolTipText(
                "Optional title. AI will occasionally refer to you by your title. "
                        + "If not provided, title will be based on your highest military rank");
        playerTitleField.setPreferredSize(new Dimension(200, 42));
        addField(this, playerTitleField, gbc, 1, 1.0);

        // Row 2: Session Theme
        nextRow(gbc);
        addLabel(this, "Session Theme:", gbc);
        playerMissionDescription = new JTextField();
        playerMissionDescription.setPreferredSize(new Dimension(200, 42));
        playerMissionDescription.setToolTipText(
                "Session theme description (optional). 'We are bounty hunters' or "
                        + "'We are deep-space explorers' or 'We are pirates'");
        installTextLimit(playerMissionDescription, 120);
        addField(this, playerMissionDescription, gbc, 1, 1.0);

        // Row 3: Journal Directory
        nextRow(gbc);
        addLabel(this, "Journal Directory:", gbc);
        journalDirField = new JTextField();
        journalDirField.setEditable(false);
        journalDirField.setPreferredSize(new Dimension(200, 42));
        journalDirField.setToolTipText(
                "Custom directory for Elite Dangerous journal files "
                        + "(optional; defaults to standard location if blank)");
        addField(this, journalDirField, gbc, 1, 0.8);
        JButton selectJournalDirButton = makeButton("Select...");
        selectJournalDirButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select Elite Dangerous Journal Directory");
            String current = playerSession.getJournalPath().toString();
            if (!current.isBlank()) chooser.setCurrentDirectory(new File(current).getParentFile());
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                playerSession.setJournalPath(path);
                journalDirField.setText(path);
            }
        });
        addField(this, selectJournalDirButton, gbc, 2, 0.2);

        // Row 4: Bindings Directory
        nextRow(gbc);
        addLabel(this, "Bindings Directory:", gbc);
        bindingsDirField = new JTextField();
        bindingsDirField.setEditable(false);
        bindingsDirField.setPreferredSize(new Dimension(200, 42));
        bindingsDirField.setToolTipText(
                "Custom directory for Elite Dangerous key bindings files "
                        + "(optional; defaults to standard location if blank)");
        addField(this, bindingsDirField, gbc, 1, 0.8);
        JButton selectBindingsDirButton = makeButton("Select...");
        selectBindingsDirButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select Elite Dangerous Bindings Directory");
            String current = playerSession.getBindingsDir().toString();
            if (!current.isBlank()) chooser.setCurrentDirectory(new File(current).getParentFile());
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                playerSession.setBindingsDir(path);
                bindingsDirField.setText(path);
            }
        });
        addField(this, selectBindingsDirButton, gbc, 2, 0.2);

        // Row 5: Save button
        nextRow(gbc);
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        JButton saveButton = makeButton("Save Player Configuration");
        saveButton.addActionListener(e -> savePlayerConfig());
        btns.add(saveButton);
        add(btns, gbc);

        // Filler
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(Box.createGlue(), gbc);
    }

    public void initData() {
        playerAltNameField.setText(playerSession.getAlternativeName() != null ? playerSession.getAlternativeName() : "");
        playerTitleField.setText(playerSession.getPlayerTitle() != null ? playerSession.getPlayerTitle() : "");
        playerMissionDescription.setText(playerSession.getPlayerMissionStatement() != null ? playerSession.getPlayerMissionStatement() : "");
        journalDirField.setText(playerSession.getJournalPath().toString());
        bindingsDirField.setText(playerSession.getBindingsDir().toString());
    }

    private void savePlayerConfig() {
        playerSession.setAlternativeName(playerAltNameField.getText());
        playerSession.setPlayerTitle(playerTitleField.getText());
        playerSession.setPlayerMissionStatement(playerMissionDescription.getText());
        playerSession.setJournalPath(journalDirField.getText());
        playerSession.setBindingsDir(bindingsDirField.getText());
        EventBusManager.publish(new AppLogEvent("Player config saved"));
        initData();
    }
}

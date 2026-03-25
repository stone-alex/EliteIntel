package elite.intel.ui.view;

import elite.intel.ai.brain.ShipCadence;
import elite.intel.ai.brain.ShipPersonality;
import elite.intel.ai.mouth.GoogleVoices;
import elite.intel.ai.mouth.kokoro.KokoroVoices;
import elite.intel.ai.mouth.subscribers.events.AiVoxDemoEvent;
import elite.intel.db.dao.ShipDao;
import elite.intel.db.managers.ShipManager;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.util.Ranks;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static elite.intel.ui.view.AppTheme.*;

public class PlayerTabPanel extends JPanel {

    private final PlayerSession playerSession = PlayerSession.getInstance();

    private JTextField playerAltNameField;
    private JTextField journalDirField;
    private JTextField bindingsDirField;
    private JScrollPane fleetScrollPane;

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

        // Row 1: Journal Directory
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

        // Row 2: Bindings Directory
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

        // Row 3: Save button
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        JButton saveButton = makeButton("Save");
        saveButton.addActionListener(e -> savePlayerConfig());
        btns.add(saveButton);
        add(btns, gbc);

        // Row 4: Fleet Management header
        nextRow(gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(16, 6, 4, 6);
        JLabel fleetLabel = new JLabel("Fleet Management");
        fleetLabel.setFont(fleetLabel.getFont().deriveFont(Font.BOLD, 13f));
        fleetLabel.setForeground(ACCENT);
        fleetLabel.setBorder(new MatteBorder(0, 0, 1, 0, BUTTON_BG));
        add(fleetLabel, gbc);

        // Row 5: Fleet grid (scrollable, takes remaining space)
        nextRow(gbc);
        gbc.insets = new Insets(0, 6, 6, 6);
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        fleetScrollPane = new JScrollPane();
        fleetScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        fleetScrollPane.setBackground(BG);
        fleetScrollPane.getViewport().setBackground(BG);
        add(fleetScrollPane, gbc);
    }

    public void initData() {
        playerAltNameField.setText(playerSession.getAlternativeName() != null ? playerSession.getAlternativeName() : "");
        journalDirField.setText(playerSession.getJournalPath().toString());
        bindingsDirField.setText(playerSession.getBindingsDir().toString());

        List<ShipDao.Ship> ships = ShipManager.getInstance().getAllShips();
        ships.sort((a, b) -> {
            String nameA = a.getShipName() == null ? "" : a.getShipName();
            String nameB = b.getShipName() == null ? "" : b.getShipName();
            return nameA.compareToIgnoreCase(nameB);
        });
        fleetScrollPane.setViewportView(buildFleetGrid(ships));
    }

    private JPanel buildFleetGrid(List<ShipDao.Ship> ships) {
        boolean useLocal = SystemSession.getInstance().useLocalTTS();
        String[] voiceOptions = useLocal
                ? Arrays.stream(KokoroVoices.values()).map(Enum::name).toArray(String[]::new)
                : Arrays.stream(GoogleVoices.values()).map(Enum::name).toArray(String[]::new);
        String[] personalityOptions = Arrays.stream(ShipPersonality.values()).map(Enum::name).toArray(String[]::new);
        String[] cadenceOptions = Arrays.stream(ShipCadence.values()).map(Enum::name).toArray(String[]::new);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        // Header row
        c.gridy = 0;
        c.weightx = 0.35;
        c.gridx = 0;
        panel.add(makeColHeader("Ship"), c);
        c.weightx = 0.25;
        c.gridx = 1;
        panel.add(makeColHeader("Voice"), c);
        c.weightx = 0.25;
        c.gridx = 2;
        panel.add(makeColHeader("Personality"), c);
        c.weightx = 0.15;
        c.gridx = 3;
        panel.add(makeColHeader("Cadence"), c);

        // Separator under header
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 4;
        c.insets = new Insets(0, 6, 4, 6);
        JSeparator sep = new JSeparator();
        sep.setForeground(BUTTON_BG);
        panel.add(sep, c);
        c.gridwidth = 1;
        c.insets = new Insets(3, 6, 3, 6);

        // Ship rows
        for (int i = 0; i < ships.size(); i++) {
            ShipDao.Ship ship = ships.get(i);
            int row = i + 2;

            c.gridy = row;
            c.gridx = 0;
            c.weightx = 0.35;
            JLabel nameLabel = new JLabel(ship.getShipName() != null ? ship.getShipName() : "Unknown");
            nameLabel.setForeground(FG);
            panel.add(nameLabel, c);

            c.gridx = 1;
            c.weightx = 0.25;
            JComboBox<String> voiceCombo = makeCombo(voiceOptions, ship.getVoice());
            voiceCombo.addActionListener(e -> {
                String voiceName = (String) voiceCombo.getSelectedItem();
                ship.setVoice(voiceName);
                String tts = "Hello " + playerSession.getPlayerName() + ", I am " + ship.getShipName() + ", at your service " + Ranks.getPlayerHonorific();
                EventBusManager.publish(new AiVoxDemoEvent(
                        tts,
                        voiceName
                ));
                ShipManager.getInstance().saveShip(ship);
            });
            panel.add(voiceCombo, c);

            c.gridx = 2;
            c.weightx = 0.25;
            JComboBox<String> personalityCombo = makeCombo(personalityOptions, ship.getPersonality());
            personalityCombo.addActionListener(e -> {
                ship.setPersonality((String) personalityCombo.getSelectedItem());
                ShipManager.getInstance().saveShip(ship);
            });
            panel.add(personalityCombo, c);

            c.gridx = 3;
            c.weightx = 0.15;
            JComboBox<String> cadenceCombo = makeCombo(cadenceOptions, ship.getCadence());
            cadenceCombo.addActionListener(e -> {
                ship.setCadence((String) cadenceCombo.getSelectedItem());
                ShipManager.getInstance().saveShip(ship);
            });
            panel.add(cadenceCombo, c);
        }

        // Push rows to top
        c.gridy = ships.size() + 2;
        c.gridx = 0;
        c.gridwidth = 4;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), c);

        return panel;
    }

    private JLabel makeColHeader(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(FG_MUTED);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        return label;
    }

    private JComboBox<String> makeCombo(String[] options, String selected) {
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setBackground(BG_PANEL);
        combo.setForeground(FG);
        if (selected != null) {
            for (String opt : options) {
                if (opt.equals(selected)) {
                    combo.setSelectedItem(selected);
                    break;
                }
            }
        }
        return combo;
    }

    private void savePlayerConfig() {
        playerSession.setAlternativeName(playerAltNameField.getText());
        playerSession.setJournalPath(journalDirField.getText());
        playerSession.setBindingsDir(bindingsDirField.getText());
        EventBusManager.publish(new AppLogEvent("Player config saved"));
        initData();
    }
}

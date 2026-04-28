package elite.intel.ui.view.settings;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static elite.intel.ui.view.AppTheme.*;

public class HonkSystemSettingsPanel implements SettingRow {

    private final String checkLabel;
    private final BooleanSupplier checkGetter;
    private final Consumer<Boolean> checkSetter;

    private final List<String> fireGroupOptions;
    private final Supplier<String> fireGroupGetter;
    private final Consumer<String> fireGroupSetter;

    private final List<Integer> triggerOptions;
    private final Supplier<Integer> triggerGetter;
    private final Consumer<Integer> triggerSetter;

    public HonkSystemSettingsPanel(String checkLabel,
                                   BooleanSupplier checkGetter,
                                   Consumer<Boolean> checkSetter,

                                   List<String> fireGroupOptions,
                                   Supplier<String> fireGroupGetter,
                                   Consumer<String> fireGroupSetter,

                                   List<Integer> triggerOptions,
                                   Supplier<Integer> triggerGetter,
                                   Consumer<Integer> triggerSetter

    ) {
        this.checkLabel = checkLabel;
        this.checkGetter = checkGetter;
        this.checkSetter = checkSetter;

        this.fireGroupOptions = fireGroupOptions;
        this.fireGroupGetter = fireGroupGetter;
        this.fireGroupSetter = fireGroupSetter;

        this.triggerOptions = triggerOptions;
        this.triggerGetter = triggerGetter;
        this.triggerSetter = triggerSetter;
    }

    @Override
    public JPanel build() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setBackground(BG);

        JCheckBox cb = new JCheckBox(checkLabel);
        boolean checked = checkGetter.getAsBoolean();
        cb.setSelected(checked);
        cb.setBackground(BG);
        cb.setForeground(FG);
        cb.setFocusPainted(false);

        JComboBox<String> fireGroupComboBox = new JComboBox<>(fireGroupOptions.toArray(new String[0]));
        fireGroupComboBox.setBackground(BG_PANEL);
        fireGroupComboBox.setForeground(FG);
        fireGroupComboBox.setEnabled(checked);

        JComboBox<Integer> triggerGroupComboBox = new JComboBox<>(triggerOptions.toArray(new Integer[0]));
        triggerGroupComboBox.setBackground(BG_PANEL);
        triggerGroupComboBox.setForeground(FG);
        triggerGroupComboBox.setEnabled(checked);


        String currentFireGroup = fireGroupGetter.get();
        if (currentFireGroup != null) fireGroupComboBox.setSelectedItem(currentFireGroup);

        Integer currentTrigger = triggerGetter.get();
        if (currentTrigger != null) triggerGroupComboBox.setSelectedItem(currentTrigger);

        cb.addActionListener(e -> {
            boolean on = cb.isSelected();
            fireGroupComboBox.setEnabled(on);
            triggerGroupComboBox.setEnabled(on);
            checkSetter.accept(on);
        });

        fireGroupComboBox.addActionListener(e -> {
            if (fireGroupComboBox.isEnabled()) {
                String val = (String) fireGroupComboBox.getSelectedItem();
                if (val != null) fireGroupSetter.accept(val);
            }
        });

        triggerGroupComboBox.addActionListener(e -> {
            if (triggerGroupComboBox.isEnabled()) {
                Integer val = (Integer) triggerGroupComboBox.getSelectedItem();
                if (val != null) {
                    triggerSetter.accept(val);
                }
            }
        });


        row.add(cb); /// Check box has label that explain action
        row.add(new JLabel("Fire Group"));
        row.add(fireGroupComboBox);
        row.add(new JLabel("Trigger"));
        row.add(triggerGroupComboBox);

        return row;
    }
}

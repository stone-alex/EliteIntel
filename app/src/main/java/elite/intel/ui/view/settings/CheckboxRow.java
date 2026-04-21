package elite.intel.ui.view.settings;

import javax.swing.*;
import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static elite.intel.ui.view.AppTheme.BG;
import static elite.intel.ui.view.AppTheme.FG;

public class CheckboxRow implements SettingRow {

    private final String label;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;

    public CheckboxRow(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public JPanel build() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setBackground(BG);

        JCheckBox cb = new JCheckBox(label);
        cb.setSelected(getter.getAsBoolean());
        cb.setBackground(BG);
        cb.setForeground(FG);
        cb.setFocusPainted(false);
        cb.addActionListener(e -> setter.accept(cb.isSelected()));

        row.add(cb);
        return row;
    }
}

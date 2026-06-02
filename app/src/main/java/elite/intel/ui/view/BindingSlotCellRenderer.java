package elite.intel.ui.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

import static elite.intel.ui.view.AppTheme.BG_PANEL;
import static elite.intel.ui.view.AppTheme.FG;
import static elite.intel.ui.view.AppTheme.FG_MUTED;
import static elite.intel.ui.view.AppTheme.SEL_BG;
import static elite.intel.ui.view.AppTheme.SEL_FG;

/**
 * Table renderer for binding rows.
 * <p>
 * The comparison uses the localized "Not defined" text because the table model
 * stores already formatted display values, not raw slot objects.
 */
class BindingSlotCellRenderer extends DefaultTableCellRenderer {
    private static final Color HOVER_BG = new Color(0x282A40);

    BindingSlotCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        boolean notDefined = elite.intel.ui.i18n.MultiLingualTextProvider
                .getText("bindings.status.notDefined")
                .equals(value);
        if (isSelected) {
            label.setBackground(SEL_BG);
            label.setForeground(SEL_FG);
            return label;
        }

        label.setBackground(isHovered(table, row) ? HOVER_BG : BG_PANEL);
        label.setForeground(notDefined ? FG_MUTED : FG);
        return label;
    }

    private boolean isHovered(JTable table, int row) {
        Object value = table.getClientProperty(BindingsGroupTableFactory.HOVER_ROW_PROPERTY);
        return value instanceof Integer hoveredRow && hoveredRow == row;
    }
}

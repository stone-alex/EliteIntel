package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

import static elite.intel.ui.view.AppTheme.FG;
import static elite.intel.ui.view.AppTheme.FG_MUTED;
import static elite.intel.ui.view.AppTheme.HUD_HOVER;
import static elite.intel.ui.view.AppTheme.HUD_PANEL_BG;

/**
 * Table renderer for binding rows.
 * <p>
 * The comparison uses the localized "Not defined" text because the table model
 * stores already formatted display values, not raw slot objects.
 */
class BindingSlotCellRenderer extends HudTable.CellRenderer {
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
        if (!isSelected) {
            label.setBackground(isHovered(table, row) ? HUD_HOVER : HUD_PANEL_BG);
            label.setForeground(notDefined ? FG_MUTED : FG);
        }
        return label;
    }

    private boolean isHovered(JTable table, int row) {
        Object value = table.getClientProperty(BindingsGroupTableFactory.HOVER_ROW_PROPERTY);
        return value instanceof Integer hoveredRow && hoveredRow == row;
    }
}

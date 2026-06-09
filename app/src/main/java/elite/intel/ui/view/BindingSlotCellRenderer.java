package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

import static elite.intel.ui.view.AppTheme.FG;
import static elite.intel.ui.view.AppTheme.HUD_DISABLED;
import static elite.intel.ui.view.AppTheme.HUD_HOVER;
import static elite.intel.ui.view.AppTheme.HUD_PANEL_BG;
import static elite.intel.ui.view.AppTheme.HUD_ROW_ALT;

/**
 * Table renderer for binding rows.
 * <p>
 * The comparison uses the localized "Not defined" text because the table model
 * stores already formatted display values, not raw slot objects.
 */
class BindingSlotCellRenderer extends HudTable.CellRenderer {
    BindingSlotCellRenderer() {
        super(2);
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
            Color rowBg = row % 2 == 0 ? HUD_PANEL_BG : HUD_ROW_ALT;
            label.setBackground(isHovered(table, row) ? HUD_HOVER : rowBg);
            label.setForeground(notDefined ? HUD_DISABLED : FG);
        }
        return label;
    }

    private boolean isHovered(JTable table, int row) {
        Object value = table.getClientProperty(BindingsGroupTableFactory.HOVER_ROW_PROPERTY);
        return value instanceof Integer hoveredRow && hoveredRow == row;
    }
}

package elite.intel.ui.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Shared table styling helper and default renderers for cockpit/HUD tables.
 */
public final class HudTable {

    private HudTable() {
    }

    /**
     * Applies the standard HUD table styling without changing the table model.
     *
     * @param table table to style
     */
    public static void style(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(AppTheme.HUD_TABLE_ROW_HEIGHT);
        table.setBackground(AppTheme.HUD_PANEL_BG);
        table.setForeground(AppTheme.FG);
        table.setGridColor(AppTheme.HUD_BORDER_DIM);
        table.setSelectionBackground(AppTheme.HUD_CYAN);
        table.setSelectionForeground(AppTheme.SEL_FG);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new CellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setBackground(AppTheme.HUD_BG);
        header.setForeground(AppTheme.FG);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, AppTheme.HUD_TABLE_HEADER_HEIGHT));
        header.setDefaultRenderer(new HeaderRenderer());
    }

    /**
     * Creates a scroll pane suitable for a HUD table.
     *
     * @param table table to wrap
     */
    public static JScrollPane scrollPane(JTable table) {
        JScrollPane scrollPane = new HudScrollPane(table);
        scrollPane.getViewport().setBackground(AppTheme.HUD_PANEL_BG);
        return scrollPane;
    }

    /**
     * Standard HUD table header renderer.
     */
    public static class HeaderRenderer extends DefaultTableCellRenderer {
        public HeaderRenderer() {
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
            label.setBackground(AppTheme.HUD_BG);
            label.setForeground(AppTheme.HUD_CYAN);
            label.setFont(label.getFont().deriveFont(Font.BOLD, AppTheme.HUD_FONT_LABEL));
            label.setBorder(new EmptyBorder(5, 8, 5, 8));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            return label;
        }
    }

    /**
     * Standard alternating-row HUD table cell renderer.
     */
    public static class CellRenderer extends DefaultTableCellRenderer {
        public CellRenderer() {
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
            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? AppTheme.HUD_PANEL_BG : AppTheme.HUD_ROW_ALT);
                label.setForeground(AppTheme.FG);
            }
            label.setBorder(new EmptyBorder(4, 8, 4, 8));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            return label;
        }
    }
}

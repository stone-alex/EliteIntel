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
        style(table, AppTheme.HUD_TABLE_ROW_HEIGHT, AppTheme.HUD_TABLE_HEADER_HEIGHT, 13f, 5, 4);
    }

    /**
     * Applies compact HUD table styling for dense cockpit data panels.
     *
     * @param table table to style
     */
    public static void styleCompact(JTable table) {
        style(table, AppTheme.HUD_TABLE_ROW_HEIGHT_COMPACT, AppTheme.HUD_TABLE_HEADER_HEIGHT_COMPACT, 12f, 3, 2);
    }

    private static void style(
            JTable table,
            int rowHeight,
            int headerHeight,
            float fontSize,
            int headerVerticalPadding,
            int cellVerticalPadding
    ) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(rowHeight);
        table.setFont(table.getFont().deriveFont(Font.PLAIN, fontSize));
        table.setBackground(AppTheme.HUD_PANEL_BG);
        table.setForeground(AppTheme.FG);
        table.setGridColor(AppTheme.HUD_BORDER_DIM);
        table.setSelectionBackground(AppTheme.HUD_CYAN);
        table.setSelectionForeground(AppTheme.SEL_FG);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultRenderer(Object.class, new CellRenderer(cellVerticalPadding));

        JTableHeader header = table.getTableHeader();
        header.setBackground(AppTheme.HUD_BG);
        header.setForeground(AppTheme.FG);
        header.setFont(header.getFont().deriveFont(Font.BOLD, Math.max(10f, fontSize - 1f)));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, headerHeight));
        header.setDefaultRenderer(new HeaderRenderer(headerVerticalPadding));
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
        private final int verticalPadding;

        public HeaderRenderer() {
            this(5);
        }

        public HeaderRenderer(int verticalPadding) {
            this.verticalPadding = verticalPadding;
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
            label.setForeground(AppTheme.FG_MUTED);
            label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D()));
            label.setBorder(new EmptyBorder(verticalPadding, 8, verticalPadding, 8));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            return label;
        }
    }

    /**
     * Standard alternating-row HUD table cell renderer.
     */
    public static class CellRenderer extends DefaultTableCellRenderer {
        private final int verticalPadding;

        public CellRenderer() {
            this(4);
        }

        public CellRenderer(int verticalPadding) {
            this.verticalPadding = verticalPadding;
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
            label.setBorder(new EmptyBorder(verticalPadding, 8, verticalPadding, 8));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            return label;
        }
    }
}

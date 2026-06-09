package elite.intel.ui.view;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

/**
 * HUD-styled tabbed pane for cockpit-dashboard navigation and data-panel switching.
 * <p>
 * Use {@link Level} to select the visual density appropriate for the context.
 * Prefer this class over {@code AppTheme.style*TabbedPane} methods for new code.
 */
public class HudTabbedPane extends JTabbedPane {

    /** Client property key marking a flat/compact tab pane that should skip the content border. */
    static final String HUD_FLAT_TABS = "eliteIntel.hud.flatTabs";

    /** Visual density and framing level for a HUD tabbed pane. */
    public enum Level {
        /** Application-level navigation bar: compact, bold, icon-ready. */
        MAIN_NAV,
        /** Sub-navigation within a screen section: compact, normal weight, no content border. */
        SECTION,
        /** Data-panel tabs for dense diagnostic views: compact, bold, no content border. */
        COMPACT,
        /** Settings-style tabs with a visible content frame and larger tab insets. */
        STANDARD
    }

    /** Creates a HUD-styled tabbed pane at the given visual level. */
    public HudTabbedPane(Level level) {
        applyStyle(this, level);
    }

    /**
     * Applies HUD tab styling to an existing {@code JTabbedPane} instance.
     * Prefer constructing a {@link HudTabbedPane} directly for new code.
     */
    public static void applyStyle(JTabbedPane tp, Level level) {
        boolean flatContent = level != Level.STANDARD;
        boolean compact = level != Level.STANDARD;
        boolean mainNavigation = level == Level.MAIN_NAV;

        tp.putClientProperty(HUD_FLAT_TABS, flatContent);
        tp.setOpaque(true);
        tp.setBackground(mainNavigation ? AppTheme.HUD_SHELL_BACKGROUND : AppTheme.HUD_CONTENT_BACKGROUND);
        tp.setForeground(AppTheme.FG);
        tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        if (mainNavigation) {
            tp.setFont(tp.getFont().deriveFont(Font.BOLD, AppTheme.HUD_FONT_LG));
        } else if (level == Level.COMPACT) {
            tp.setFont(tp.getFont().deriveFont(Font.BOLD, AppTheme.HUD_FONT_SECTION));
        } else if (level == Level.SECTION) {
            tp.setFont(tp.getFont().deriveFont(Font.PLAIN, AppTheme.HUD_FONT_SECTION));
        }

        tp.setUI(new HudTabbedPaneUi(flatContent, compact, mainNavigation));
    }

    private static class HudTabbedPaneUi extends BasicTabbedPaneUI {

        private final boolean flatContent;
        private final boolean compact;
        private final boolean mainNavigation;

        HudTabbedPaneUi(boolean flatContent, boolean compact, boolean mainNavigation) {
            this.flatContent = flatContent;
            this.compact = compact;
            this.mainNavigation = mainNavigation;
        }

        @Override
        protected void installDefaults() {
            super.installDefaults();
            contentBorderInsets = flatContent ? new Insets(0, 0, 0, 0) : new Insets(1, 1, 1, 1);
            if (mainNavigation) {
                tabInsets = new Insets(9, 14, 9, 14);
            } else if (compact) {
                tabInsets = new Insets(3, 9, 3, 9);
            } else {
                tabInsets = new Insets(8, 14, 8, 14);
            }
            selectedTabPadInsets = new Insets(1, 1, 1, 1);
            focus = AppTheme.HUD_CYAN;
        }

        @Override
        protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
            int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
            g.setColor(mainNavigation ? AppTheme.HUD_SHELL_BACKGROUND : AppTheme.HUD_CONTENT_BACKGROUND);
            g.fillRect(0, 0, tabPane.getWidth(), tabAreaHeight);
            super.paintTabArea(g, tabPlacement, selectedIndex);
            if (mainNavigation) {
                g.setColor(AppTheme.HUD_BORDER_DIM);
                g.drawLine(0, tabAreaHeight - 1, tabPane.getWidth() - 1, tabAreaHeight - 1);
            }
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            Color background = mainNavigation || compact ? AppTheme.HUD_SHELL_BACKGROUND : AppTheme.HUD_CONTENT_BACKGROUND;
            g.setColor(compact ? background : isSelected ? AppTheme.HUD_PANEL_BG_ALT : background);
            g.fillRect(x, y, w, h);
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
            if (!isSelected) return;
            // Orange underline
            g.setColor(AppTheme.ACCENT);
            g.fillRect(x, y + h - 3, w, 3);
            // Subtle side ticks centred vertically — main nav only
            if (mainNavigation) {
                int tickLen = h * 2 / 3;
                int tickTop = y + (h - tickLen) / 2;
                int tickBottom = tickTop + tickLen - 1;
                g.setColor(AppTheme.HUD_BORDER_DIM);
                g.drawLine(x, tickTop, x, tickBottom);
                g.drawLine(x + w - 1, tickTop, x + w - 1, tickBottom);
            }
        }

        @Override
        protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex,
                                   String title, Icon icon, Rectangle tabRect, Rectangle iconRect,
                                   Rectangle textRect, boolean isSelected) {
            super.layoutLabel(tabPlacement, metrics, tabIndex,
                    title != null ? title.toUpperCase() : title,
                    icon, tabRect, iconRect, textRect, isSelected);
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                 int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            g.setFont(font);
            String upper = title != null ? title.toUpperCase() : "";
            if (!tabPane.isEnabled() || !tabPane.isEnabledAt(tabIndex)) {
                g.setColor(AppTheme.HUD_DISABLED);
            } else {
                g.setColor(isSelected ? AppTheme.ACCENT : AppTheme.FG_MUTED);
            }
            g.drawString(upper, textRect.x, textRect.y + metrics.getAscent());
        }

        @Override
        protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
            int base = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
            String title = tabPane.getTitleAt(tabIndex);
            if (title != null && !title.isEmpty()) {
                base += SwingUtilities.computeStringWidth(metrics, title.toUpperCase())
                        - SwingUtilities.computeStringWidth(metrics, title);
            }
            return base;
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            if (flatContent) {
                return;
            }
            Insets in = tabPane.getInsets();
            int top = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
            int x = in.left;
            int y = in.top + top;
            int w = tabPane.getWidth() - in.left - in.right;
            int h = tabPane.getHeight() - y - in.bottom;
            g.setColor(AppTheme.HUD_BORDER_DIM);
            g.drawRect(x, y, Math.max(0, w - 1), Math.max(0, h - 1));
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                           int tabIndex, Rectangle iconRect,
                                           Rectangle textRect, boolean isSelected) {
            // no dotted focus ring
        }
    }
}

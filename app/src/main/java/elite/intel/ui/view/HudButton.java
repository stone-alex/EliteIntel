package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

/**
 * HUD-styled Swing button with consistent cockpit fill, border, and hover states.
 */
public class HudButton extends JButton {

    private static final Color PRIMARY_FILL          = new Color(0xB04000);
    private static final Color PRIMARY_FILL_HOVER    = new Color(0xCC4D00);
    private static final Color PRIMARY_FILL_PRESSED  = new Color(0xFF6000);
    private static final Color PRIMARY_FILL_DISABLED = new Color(0x3A1E0A);

    private final boolean primary;

    /**
     * Creates a reusable HUD button.
     *
     * @param label   visible button text
     * @param primary true for the orange primary treatment, false for a subdued treatment
     */
    public HudButton(String label, boolean primary) {
        super(label != null ? label.toUpperCase() : "");
        this.primary = primary;
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setForeground(primary ? AppTheme.BUTTON_FG : AppTheme.FG);
        setFont(getFont().deriveFont(Font.BOLD, 12f));
        setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        setPreferredSize(new Dimension(Math.max(90, getPreferredSize().width), AppTheme.HUD_BUTTON_HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            int w = getWidth();
            int h = getHeight();
            ButtonModel model = getModel();

            if (primary) {
                paintPrimary(g2, w, h, model);
            } else {
                paintSecondary(g2, w, h, model);
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    private void paintPrimary(Graphics2D g2, int w, int h, ButtonModel model) {
        if (!isEnabled()) {
            g2.setColor(PRIMARY_FILL_DISABLED);
            g2.fillRect(0, 0, w - 1, h - 1);
            g2.setColor(AppTheme.ACCENT);
            g2.drawRect(0, 0, w - 1, h - 1);
            return;
        }
        Color fill = model.isPressed() ? PRIMARY_FILL_PRESSED
                   : model.isRollover() ? PRIMARY_FILL_HOVER
                   : PRIMARY_FILL;
        g2.setColor(fill);
        g2.fillRect(0, 0, w - 1, h - 1);
        g2.setColor(AppTheme.ACCENT);
        g2.drawRect(0, 0, w - 1, h - 1);
        Color glow = new Color(AppTheme.ACCENT.getRed(), AppTheme.ACCENT.getGreen(),
                AppTheme.ACCENT.getBlue(), 70);
        g2.setColor(glow);
        g2.drawRect(1, 1, w - 3, h - 3);
    }

    private void paintSecondary(Graphics2D g2, int w, int h, ButtonModel model) {
        Color fill = !isEnabled() ? AppTheme.HUD_PANEL_BG
                   : model.isPressed() ? AppTheme.HUD_PANEL_BG_ALT.darker()
                   : model.isRollover() ? AppTheme.HUD_HOVER
                   : AppTheme.HUD_PANEL_BG;
        Color border = !isEnabled() ? AppTheme.HUD_BORDER_DIM : AppTheme.HUD_BORDER;
        g2.setColor(fill);
        g2.fillRect(0, 0, w - 1, h - 1);
        g2.setColor(border);
        g2.drawRect(0, 0, w - 1, h - 1);
    }
}

package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

/**
 * HUD-styled Swing button with consistent cockpit fill, border, and hover states.
 */
public class HudButton extends JButton {

    private final boolean primary;

    /**
     * Creates a reusable HUD button.
     *
     * @param label visible button text
     * @param primary true for the orange primary treatment, false for a subdued treatment
     */
    public HudButton(String label, boolean primary) {
        super(label);
        this.primary = primary;
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setForeground(primary ? AppTheme.BUTTON_FG : AppTheme.FG);
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        setPreferredSize(new Dimension(Math.max(96, getPreferredSize().width), AppTheme.HUD_BUTTON_HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ButtonModel model = getModel();
            Color border = primary ? AppTheme.ACCENT : AppTheme.HUD_BORDER;
            Color fill = primary ? new Color(0x6A3A08) : AppTheme.HUD_PANEL_BG_ALT;
            if (!isEnabled()) {
                border = AppTheme.HUD_DISABLED;
                fill = AppTheme.HUD_PANEL_BG;
            } else if (model.isPressed()) {
                fill = fill.darker();
            } else if (model.isRollover()) {
                fill = primary ? new Color(0x8A4B08) : AppTheme.HUD_HOVER;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}

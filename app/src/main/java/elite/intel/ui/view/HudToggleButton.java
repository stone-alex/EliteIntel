package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

/**
 * Toggle variant of the HUD button used for service and on/off controls.
 */
public class HudToggleButton extends JToggleButton {

    /**
     * Creates a HUD toggle button with selected-state accent styling.
     *
     * @param label visible button text
     */
    public HudToggleButton(String label) {
        super(label);
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setForeground(AppTheme.FG);
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        setPreferredSize(new Dimension(Math.max(112, getPreferredSize().width), AppTheme.HUD_BUTTON_HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ButtonModel model = getModel();
            Color border = isSelected() ? AppTheme.ACCENT : AppTheme.HUD_BORDER;
            Color fill = isSelected() ? new Color(0x55340C) : AppTheme.HUD_PANEL_BG_ALT;
            if (!isEnabled()) {
                border = AppTheme.HUD_DISABLED;
                fill = AppTheme.HUD_PANEL_BG;
            } else if (model.isPressed()) {
                fill = fill.darker();
            } else if (model.isRollover()) {
                fill = isSelected() ? new Color(0x70440F) : AppTheme.HUD_HOVER;
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

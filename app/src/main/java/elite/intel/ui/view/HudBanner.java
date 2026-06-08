package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable compact banner for warnings, informational messages, and status hints.
 */
public class HudBanner extends HudPanel {

    /**
     * Creates a banner with semantic colour treatment.
     *
     * @param text localized message text
     * @param state semantic state colour
     */
    public HudBanner(String text, StatusBadge.State state) {
        super(new BorderLayout(), colorFor(state));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorFor(state), AppTheme.HUD_BORDER_THICKNESS, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JLabel label = new JLabel(text == null ? "" : text);
        label.setForeground(colorFor(state));
        label.setFont(label.getFont().deriveFont(Font.BOLD, AppTheme.HUD_FONT_LABEL));
        add(label, BorderLayout.CENTER);
    }

    private static Color colorFor(StatusBadge.State state) {
        if (state == null) return AppTheme.HUD_CYAN;
        return switch (state) {
            case OK -> AppTheme.HUD_OK;
            case STANDBY -> AppTheme.HUD_WARN;
            case OFFLINE -> AppTheme.HUD_DANGER;
            case INFO -> AppTheme.HUD_CYAN;
        };
    }
}

package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

/**
 * Compact cockpit-style application header that hosts global status badges.
 */
public class TopStatusBar extends HudPanel {

    private final StatusBadge servicesBadge;

    /**
     * Creates the top-level HUD strip for the main application window.
     *
     * @param appName localized application name
     * @param version application version displayed as build metadata
     * @param servicesRunning initial services state
     */
    public TopStatusBar(String appName, String version, boolean servicesRunning) {
        super(new BorderLayout(AppTheme.HUD_GAP, 0), AppTheme.HUD_CYAN);
        setPreferredSize(new Dimension(0, AppTheme.HUD_TOP_BAR_HEIGHT));
        setMinimumSize(new Dimension(0, AppTheme.HUD_TOP_BAR_HEIGHT));

        add(buildBrand(appName, version), BorderLayout.WEST);

        JPanel badges = new JPanel();
        badges.setOpaque(false);
        badges.setLayout(new BoxLayout(badges, BoxLayout.X_AXIS));
        servicesBadge = new StatusBadge("", StatusBadge.State.INFO);
        setServicesRunning(servicesRunning);
        badges.add(servicesBadge);

        add(badges, BorderLayout.EAST);
    }

    /**
     * Updates the services badge without starting or stopping any service.
     *
     * @param running current service state
     */
    public void setServicesRunning(boolean running) {
        servicesBadge.setStatus(
                running ? "SERVICES ON" : "SERVICES OFF",
                running ? StatusBadge.State.OK : StatusBadge.State.STANDBY
        );
    }

    private JPanel buildBrand(String appName, String version) {
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(appName == null ? "" : appName.toUpperCase());
        title.setForeground(AppTheme.FG);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel meta = new JLabel("BUILD " + (version == null || version.isBlank() ? "UNKNOWN" : version));
        meta.setForeground(AppTheme.HUD_CYAN);
        meta.setFont(meta.getFont().deriveFont(Font.PLAIN, 11f));
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(title);
        brand.add(Box.createVerticalStrut(2));
        brand.add(meta);
        return brand;
    }
}

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
        super(new BorderLayout(AppTheme.HUD_GAP, 0), AppTheme.HUD_CYAN, Variant.FLAT);
        setPreferredSize(new Dimension(0, AppTheme.HUD_TOP_BAR_HEIGHT));
        setMinimumSize(new Dimension(0, AppTheme.HUD_TOP_BAR_HEIGHT));
        setBackground(AppTheme.HUD_SHELL_BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.HUD_BORDER_DIM),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

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

    private JComponent buildBrand(String appName, String version) {
        String safeName = appName == null ? "" : appName.toUpperCase();
        String safeVersion = version == null || version.isBlank() ? "UNKNOWN" : version;
        JLabel brand = new JLabel(safeName + "  " + safeVersion);
        brand.setForeground(AppTheme.FG);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 13f));
        return brand;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(AppTheme.HUD_SHELL_BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}

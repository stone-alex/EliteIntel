package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable titled HUD section/card for grouping related controls and telemetry.
 */
public class HudSection extends HudPanel {

    private final JPanel body;

    /**
     * Creates a titled section with a supplied body layout.
     *
     * @param title localized section title
     * @param bodyLayout layout manager used by the content body
     */
    public HudSection(String title, LayoutManager bodyLayout) {
        super(new BorderLayout(0, AppTheme.HUD_GAP));
        JLabel header = AppTheme.hudSectionLabel(title == null ? "" : title.toUpperCase());
        add(header, BorderLayout.NORTH);
        body = AppTheme.transparentPanel(bodyLayout);
        add(body, BorderLayout.CENTER);
    }

    /**
     * Returns the mutable content body for adding section controls.
     */
    public JPanel body() {
        return body;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension preferred = getPreferredSize();
        return new Dimension(Integer.MAX_VALUE, preferred.height);
    }
}

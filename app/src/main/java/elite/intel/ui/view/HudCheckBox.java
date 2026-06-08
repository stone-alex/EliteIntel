package elite.intel.ui.view;

import javax.swing.*;

/**
 * HUD-styled checkbox that preserves native Swing checkbox behaviour.
 */
public class HudCheckBox extends JCheckBox {

    /**
     * Creates a HUD checkbox.
     *
     * @param label visible checkbox text
     * @param selected initial selected state
     */
    public HudCheckBox(String label, boolean selected) {
        super(label, selected);
        AppTheme.styleCheckBox(this);
    }
}

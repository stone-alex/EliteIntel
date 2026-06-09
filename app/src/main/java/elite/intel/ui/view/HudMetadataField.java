package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

/**
 * Compact read-only HUD field for metadata values that should read as bounded surfaces, not editable inputs.
 */
public class HudMetadataField extends JTextField {

    /**
     * Creates an empty non-editable metadata field.
     */
    public HudMetadataField() {
        setEditable(false);
        AppTheme.styleMetadataField(this);
        setFont(getFont().deriveFont(Font.PLAIN, 12f));
        setPreferredSize(new Dimension(0, AppTheme.HUD_FORM_ROW_HEIGHT_COMPACT));
    }
}

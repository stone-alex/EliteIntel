package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

/**
 * HUD-styled combo box with shared dark input colours and readable cell rendering.
 *
 * @param <E> option type
 */
public class HudComboBox<E> extends JComboBox<E> {

    /**
     * Creates a HUD combo box using the supplied values.
     *
     * @param values selectable values
     */
    public HudComboBox(E[] values) {
        super(values);
        AppTheme.styleComboBox(this);
        setRenderer(new HudComboRenderer<>());
    }

    private static final class HudComboRenderer<E> extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBackground(isSelected ? AppTheme.HUD_CYAN : AppTheme.HUD_PANEL_BG_ALT);
            label.setForeground(isSelected ? AppTheme.SEL_FG : AppTheme.FG);
            label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            return label;
        }
    }
}

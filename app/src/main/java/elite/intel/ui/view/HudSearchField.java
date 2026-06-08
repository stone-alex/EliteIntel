package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable HUD search input with placeholder text and an embedded clear action.
 */
public class HudSearchField extends JPanel {

    private final JTextField textField;

    /**
     * Creates a search field wrapper that owns the HUD border and clear button.
     *
     * @param placeholder localized placeholder and tooltip text
     * @param clearTooltip localized tooltip for the clear button
     */
    public HudSearchField(String placeholder, String clearTooltip) {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(AppTheme.HUD_PANEL_BG_ALT);
        setBorder(AppTheme.hudFieldBorder());

        textField = new PlaceholderTextField(placeholder);
        textField.setOpaque(false);
        textField.setBorder(BorderFactory.createEmptyBorder());
        AppTheme.styleTextComponent(textField);
        textField.setOpaque(false);
        textField.setBorder(BorderFactory.createEmptyBorder());

        JButton clearButton = new JButton("×");
        clearButton.setToolTipText(clearTooltip);
        clearButton.setOpaque(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setBorderPainted(false);
        clearButton.setFocusable(false);
        clearButton.setForeground(AppTheme.FG_MUTED);
        clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearButton.setMargin(new Insets(0, 8, 0, 8));
        clearButton.addActionListener(event -> textField.setText(""));

        add(textField, BorderLayout.CENTER);
        add(clearButton, BorderLayout.EAST);
    }

    /**
     * Returns the underlying text field so callers can attach document listeners.
     */
    public JTextField textField() {
        return textField;
    }

    private static final class PlaceholderTextField extends JTextField {
        private final String placeholder;

        private PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setToolTipText(placeholder);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (!getText().isEmpty() || placeholder == null || placeholder.isBlank()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setColor(AppTheme.FG_MUTED);
                FontMetrics metrics = g2.getFontMetrics();
                Insets insets = getInsets();
                int x = insets.left + 2;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g2.drawString(placeholder, x, y);
            } finally {
                g2.dispose();
            }
        }
    }
}

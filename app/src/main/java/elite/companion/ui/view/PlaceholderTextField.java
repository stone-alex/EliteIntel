package elite.companion.ui.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * A JTextField that shows gray placeholder (hint) text when empty.
 */
public class PlaceholderTextField extends JTextField {
    private String hint;
    private Color hintColor = new Color(150, 150, 150); // light gray
    private boolean showHintWhileFocused = true;

    public PlaceholderTextField() {
        this(null);
    }

    public PlaceholderTextField(String hint) {
        super();
        this.hint = hint;
        installRepaintOnChanges();
    }

    public void setHint(String hint) {
        this.hint = hint;
        repaint();
    }

    public void setHintColor(Color hintColor) {
        this.hintColor = hintColor;
        repaint();
    }

    public void setShowHintWhileFocused(boolean show) {
        this.showHintWhileFocused = show;
        repaint();
    }

    private void installRepaintOnChanges() {
        getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                repaint();
            }

            @Override public void removeUpdate(DocumentEvent e) {
                repaint();
            }

            @Override public void changedUpdate(DocumentEvent e) {
                repaint();
            }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (!showHintWhileFocused) repaint();
            }

            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (!showHintWhileFocused) repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        boolean show = (hint != null && !hint.isEmpty())
                && getText().isEmpty()
                && (showHintWhileFocused || !hasFocus());

        if (!show) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(hintColor);

            Insets in = getInsets();
            FontMetrics fm = g2.getFontMetrics(getFont());
            int x = in.left + 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(hint, x, y);
        } finally {
            g2.dispose();
        }
    }
}
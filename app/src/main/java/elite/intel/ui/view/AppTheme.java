package elite.intel.ui.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.*;
import java.awt.*;

/**
 * Shared colors, component factories, and layout helpers for the dark UI theme.
 */
class AppTheme {

    static final Color BG = new Color(0x141622);
    static final Color LOG_BG = new Color(0x171927);
    static final Color BG_PANEL = new Color(0x1F2032);
    static final Color FG = new Color(0xE6E6E6);
    static final Color BUTTON_FG = new Color(0xFFFFFF);
    static final Color BUTTON_BG = new Color(0x03529F);
    static final Color FG_MUTED = new Color(0xB0B0B0);
    static final Color ACCENT = new Color(0xFF8C00);
    static final Color CONSOLE_FG = new Color(0xE0FFEF);
    static final Color SEL_BG = new Color(0xE0FFEF);
    static final Color SEL_FG = new Color(0x13181D);
    static final Color TAB_UNSELECTED = new Color(0x141622);
    static final Color TAB_SELECTED = new Color(0x141622);
    static final Color DISABLED_FG = new Color(0x8B0101);

    // ── Button factories ──────────────────────────────────────────────────────

    static JButton makeButton(String label) {
        return makeRoundButton(label, BUTTON_BG);
    }

    static JButton makeButtonSubtle(String label) {
        return makeRoundButton(label, BG_PANEL);
    }

    private static JButton makeRoundButton(String label, Color baseColor) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = baseColor;
                    ButtonModel m = getModel();
                    if (m.isPressed()) base = base.darker();
                    else if (m.isRollover()) base = base.brighter();
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(btn);
        return btn;
    }

    static JToggleButton makeToggleButton(String label) {
        JToggleButton btn = new JToggleButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = BUTTON_BG;
                    ButtonModel m = getModel();
                    if (m.isPressed()) base = base.darker();
                    else if (m.isRollover()) base = base.brighter();
                    g2.setColor(base);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleButton(btn);
        return btn;
    }

    static void styleButton(AbstractButton b) {
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setForeground(FG);
        b.setBackground(BUTTON_BG);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BUTTON_BG, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
    }

    // ── Tabbed pane ───────────────────────────────────────────────────────────

    static void styleTabbedPane(JTabbedPane tp) {
        tp.setOpaque(true);
        tp.setBackground(BG);
        tp.setForeground(FG);
        tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tp.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                contentBorderInsets = new Insets(1, 1, 1, 1);
                tabInsets = new Insets(8, 14, 8, 14);
                selectedTabPadInsets = new Insets(1, 1, 1, 1);
                focus = ACCENT;
            }

            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                g.setColor(BG);
                g.fillRect(0, 0, tabPane.getWidth(),
                        calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight));
                super.paintTabArea(g, tabPlacement, selectedIndex);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                g.setColor(isSelected ? TAB_SELECTED : TAB_UNSELECTED);
                g.fillRect(x, y, w, h);
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
                if (isSelected) {
                    g.setColor(BUTTON_BG);
                    g.fillRect(x, y + h - 3, w, 3);
                }
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                Insets in = tabPane.getInsets();
                int top = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                int x = in.left;
                int y = in.top + top;
                int w = tabPane.getWidth() - in.left - in.right;
                int h = tabPane.getHeight() - y - in.bottom;
                g.drawRect(x, y, Math.max(0, w - 1), Math.max(0, h - 1));
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                               int tabIndex, Rectangle iconRect,
                                               Rectangle textRect, boolean isSelected) {
                // no dotted focus ring
            }
        });
    }

    // ── Dark palette ──────────────────────────────────────────────────────────

    static void applyDarkPalette(Component c) {
        if (c == null) return;

        if (c instanceof JPanel || c instanceof JTabbedPane || c instanceof JScrollPane) {
            c.setBackground(BG);
            c.setForeground(FG);
        } else {
            c.setBackground(c instanceof JTextComponent ? BG_PANEL : BG);
            c.setForeground(FG);
        }

        if (c instanceof JTextArea) {
            c.setBackground(LOG_BG);
            c.setForeground(CONSOLE_FG);
        }

        if (c instanceof JTextComponent tc) {
            tc.setCaretColor(FG);
            tc.setSelectionColor(SEL_BG);
            tc.setSelectedTextColor(SEL_FG);
            tc.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ACCENT, 1, true),
                    new EmptyBorder(6, 8, 6, 8)
            ));
        }

        if (c instanceof JButton b) {
            b.setBackground(BUTTON_BG);
            b.setForeground(BUTTON_FG);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BUTTON_BG, 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        }

        if (c instanceof JCheckBox cb) {
            cb.setBackground(BG);
            cb.setForeground(FG);
        }

        if (c instanceof JTabbedPane tp) {
            tp.setBackground(BG);
            tp.setForeground(FG);
            tp.setOpaque(true);
        }

        if (c instanceof JScrollPane sp) {
            sp.getViewport().setBackground(BG);
            sp.setBorder(new LineBorder(ACCENT, 1, true));
        }

        if (c instanceof JEditorPane ep) {
            ep.setBackground(Color.WHITE);
            ep.setForeground(Color.BLACK);
        }

        if (c instanceof Container cont) {
            for (Component child : cont.getComponents()) {
                applyDarkPalette(child);
            }
        }
    }

    // ── GridBagLayout helpers ─────────────────────────────────────────────────

    static GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        return gbc;
    }

    static void nextRow(GridBagConstraints gbc) {
        gbc.gridy++;
    }

    static void addLabel(JPanel panel, String text, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(220, 42));
        panel.add(label, gbc);
    }

    static void addLabel(JPanel panel, String text, GridBagConstraints gbc, int col, double weightX) {
        gbc.gridx = col;
        gbc.weightx = weightX;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel comp = new JLabel(text);
        comp.setPreferredSize(new Dimension(0, comp.getPreferredSize().height));
        panel.add(comp, gbc);
    }

    static void addField(JPanel panel, JComponent comp, GridBagConstraints gbc, int col, double weightX) {
        gbc.gridx = col;
        gbc.weightx = weightX;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        comp.setPreferredSize(new Dimension(0, comp.getPreferredSize().height));
        panel.add(comp, gbc);
    }

    static void addCheck(JPanel panel, JCheckBox check, GridBagConstraints gbc) {
        gbc.gridx = 2;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(check, gbc);
    }

    static void addNestedPanel(JPanel parent, JPanel child, String title) {
        parent.add(new JLabel(title));
        parent.add(child);
    }

    static void bindLock(JCheckBox lockCheck, JComponent field) {
        Runnable apply = () -> {
            boolean locked = lockCheck.isSelected();
            if (field instanceof JTextComponent tc) {
                tc.setEnabled(!locked);
            } else {
                field.setEnabled(!locked);
            }
        };
        lockCheck.addItemListener(e -> apply.run());
        apply.run();
    }

    static void installTextLimit(JTextField field, int maxChars) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;
                int newLen = fb.getDocument().getLength() + string.length();
                if (newLen <= maxChars) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    int allowed = maxChars - fb.getDocument().getLength();
                    if (allowed > 0) super.insertString(fb, offset, string.substring(0, allowed), attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    super.replace(fb, offset, length, null, attrs);
                    return;
                }
                int newLen = fb.getDocument().getLength() - length + text.length();
                if (newLen <= maxChars) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    int allowed = maxChars - (fb.getDocument().getLength() - length);
                    if (allowed > 0) super.replace(fb, offset, length, text.substring(0, allowed), attrs);
                }
            }
        });
    }
}

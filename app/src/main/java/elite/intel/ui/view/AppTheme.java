package elite.intel.ui.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Shared colors, component factories, and layout helpers for the dark UI theme.
 */
public class AppTheme {
    private static final String HUD_LOCKED_FOREGROUND = "eliteIntel.hud.lockedForeground";

    public static final Color BG = new Color(0x151519);
    public static final Color LOG_BG = new Color(0x171927);
    public static final Color BG_PANEL = new Color(0x1F2032);
    public static final Color FG = new Color(0xE6E6E6);
    public static final Color BUTTON_FG = new Color(0xFFFFFF);
    public static final Color BUTTON_BG = new Color(0x03529F);
    public static final Color FG_MUTED = new Color(0xB0B0B0);
    public static final Color ACCENT = new Color(0xFF8C00);
    public static final Color ACCENT_CYAN = new Color(0x33D7E8);
    public static final Color CONSOLE_FG = new Color(0xE0FFEF);
    public static final Color SEL_BG = new Color(0xE0FFEF);
    public static final Color SEL_FG = new Color(0x13181D);
    public static final Color TAB_UNSELECTED = new Color(0x151519);
    public static final Color TAB_SELECTED = new Color(0x151519);
    public static final Color DISABLED_FG = new Color(0x8B0101);

    // -- HUD design tokens -----------------------------------------------------

    public static final Color HUD_BG = new Color(0x090D12);
    public static final Color HUD_PANEL_BG = new Color(0x101721);
    public static final Color HUD_PANEL_BG_ALT = new Color(0x151E2B);
    public static final Color HUD_BORDER = new Color(0x2D5C66);
    public static final Color HUD_BORDER_DIM = new Color(0x24313A);
    public static final Color HUD_CYAN = new Color(0x33D7E8);
    public static final Color HUD_OK = new Color(0x4FC56B);
    public static final Color HUD_WARN = new Color(0xFFB347);
    public static final Color HUD_DANGER = new Color(0xD94F4F);
    public static final Color HUD_DISABLED = new Color(0x65717A);
    public static final Color HUD_ROW_ALT = new Color(0x0E1420);
    public static final Color HUD_HOVER = new Color(0x182838);

    public static final int HUD_GAP = 8;
    public static final int HUD_PADDING = 10;
    public static final int HUD_PADDING_SMALL = 6;
    public static final int HUD_BORDER_THICKNESS = 1;
    public static final int HUD_PANEL_ARC = 12;
    public static final int HUD_TOP_BAR_HEIGHT = 58;
    public static final int HUD_BADGE_HEIGHT = 24;
    public static final int HUD_FIELD_HEIGHT = 34;
    public static final int HUD_BUTTON_HEIGHT = 34;
    public static final int HUD_TABLE_ROW_HEIGHT = 34;
    public static final int HUD_TABLE_HEADER_HEIGHT = 30;
    public static final int HUD_ICON_MAIN = 42;
    public static final int HUD_ICON_SMALL = 28;
    public static final float HUD_FONT_SECTION = 12f;
    public static final float HUD_FONT_LABEL = 13f;
    public static final float HUD_FONT_BADGE = 11f;

    // -- Button factories ------------------------------------------------------

    public static JButton makeButton(String label) {
        return new HudButton(label, true);
    }

    public static JButton makeButtonSubtle(String label) {
        return new HudButton(label, false);
    }

    /**
     * Creates a HUD-styled toggle button for on/off controls.
     */
    public static JToggleButton makeToggleButton(String label) {
        return new HudToggleButton(label);
    }

    /**
     * Creates a HUD-styled checkbox preserving standard Swing checkbox behaviour.
     */
    public static JCheckBox makeCheckBox(String label, boolean selected) {
        return new HudCheckBox(label, selected);
    }

    /**
     * Creates a HUD-styled single-line text field.
     */
    public static JTextField makeTextField() {
        return new HudTextField();
    }

    /**
     * Creates a HUD-styled password field for secret inputs.
     */
    public static JPasswordField makePasswordField() {
        return new HudPasswordField();
    }

    /**
     * Creates a HUD-styled multi-line text area for logs and details.
     */
    public static JTextArea makeTextArea(int rows, int columns) {
        return new HudTextArea(rows, columns);
    }

    /**
     * Creates a HUD-styled combo box with readable dropdown cells.
     */
    public static <E> JComboBox<E> makeComboBox(E[] values) {
        return new HudComboBox<>(values);
    }

    public static void styleButton(AbstractButton b) {
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setForeground(FG);
        b.setBackground(BUTTON_BG);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BUTTON_BG, HUD_BORDER_THICKNESS, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
    }

    /**
     * Creates the standard border used by reusable cockpit/HUD panels.
     */
    public static Border hudBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(HUD_BORDER_DIM, HUD_BORDER_THICKNESS, true),
                new EmptyBorder(HUD_PADDING, HUD_PADDING, HUD_PADDING, HUD_PADDING)
        );
    }

    /**
     * Creates a compact label for HUD section titles without changing the global UI font.
     */
    public static JLabel hudSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ACCENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, HUD_FONT_SECTION));
        label.putClientProperty(HUD_LOCKED_FOREGROUND, Boolean.TRUE);
        return label;
    }

    /**
     * Creates the standard HUD input border used by text fields and combo boxes.
     */
    public static Border hudFieldBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(HUD_BORDER, HUD_BORDER_THICKNESS, true),
                new EmptyBorder(5, 8, 5, 8)
        );
    }

    /**
     * Creates a transparent panel with the supplied layout for HUD composition.
     */
    public static JPanel transparentPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Wraps a component in the standard HUD scroll pane.
     */
    public static JScrollPane hudScrollPane(Component view) {
        return new HudScrollPane(view);
    }

    /**
     * Applies the standard HUD treatment to text components without replacing the component instance.
     */
    public static void styleTextComponent(JTextComponent tc) {
        tc.setBackground(HUD_PANEL_BG_ALT);
        tc.setForeground(FG);
        tc.setCaretColor(HUD_CYAN);
        tc.setSelectionColor(HUD_CYAN);
        tc.setSelectedTextColor(SEL_FG);
        tc.setBorder(hudFieldBorder());
    }

    /**
     * Applies the standard HUD treatment to combo boxes without replacing the model or renderer.
     */
    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(HUD_PANEL_BG_ALT);
        comboBox.setForeground(FG);
        comboBox.setBorder(hudFieldBorder());
        comboBox.setFocusable(true);
    }

    /**
     * Applies the standard HUD treatment to checkbox-like buttons.
     */
    public static void styleCheckBox(AbstractButton checkBox) {
        checkBox.setOpaque(false);
        checkBox.setForeground(FG);
        checkBox.setFocusPainted(false);
    }

    /**
     * Applies the standard HUD treatment to scroll panes and their viewport.
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(HUD_BG);
        scrollPane.getViewport().setBackground(HUD_PANEL_BG);
        scrollPane.setBorder(new LineBorder(HUD_BORDER_DIM, HUD_BORDER_THICKNESS, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }

    /**
     * Applies the standard HUD treatment to sliders.
     */
    public static void styleSlider(JSlider slider) {
        slider.setOpaque(false);
        slider.setForeground(FG_MUTED);
        slider.setBackground(HUD_PANEL_BG);
    }

    /**
     * Applies shared HUD table metrics and renderers to an existing table.
     */
    public static void styleTable(JTable table) {
        HudTable.style(table);
    }

    /**
     * Loads and scales an image resource relative to the supplied owner class.
     */
    public static ImageIcon scaledIcon(Class<?> owner, String resource, int size) {
        return scaledIcon(owner, resource, size, size);
    }

    /**
     * Loads and scales an image resource relative to the supplied owner class.
     */
    public static ImageIcon scaledIcon(Class<?> owner, String resource, int width, int height) {
        return new ImageIcon(
                new ImageIcon(java.util.Objects.requireNonNull(owner.getResource(resource)))
                        .getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH)
        );
    }

    // -- Tabbed pane -----------------------------------------------------------

    public static void styleTabbedPane(JTabbedPane tp) {
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
                focus = HUD_CYAN;
            }

            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                g.setColor(HUD_BG);
                g.fillRect(0, 0, tabPane.getWidth(),
                        calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight)
                );
                super.paintTabArea(g, tabPlacement, selectedIndex);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                g.setColor(isSelected ? HUD_PANEL_BG_ALT : HUD_BG);
                g.fillRect(x, y, w, h);
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
                if (isSelected) {
                    g.setColor(isSelected ? ACCENT : HUD_BORDER);
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
                g.setColor(HUD_BORDER_DIM);
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

    // -- Dark palette ----------------------------------------------------------

    public static void applyDarkPalette(Component c) {
        if (c == null) return;
        boolean lockForeground = c instanceof JComponent jc
                && Boolean.TRUE.equals(jc.getClientProperty(HUD_LOCKED_FOREGROUND));

        if (c instanceof TopStatusBar || c instanceof HudPanel || c instanceof StatusBadge) {
            // HUD primitives own their painting and state colours.
        } else if (c instanceof JPanel || c instanceof JTabbedPane || c instanceof JScrollPane) {
            c.setBackground(BG);
            if (!lockForeground) c.setForeground(FG);
        } else {
            c.setBackground(c instanceof JTextComponent ? BG_PANEL : BG);
            if (!lockForeground) c.setForeground(FG);
        }

        if (c instanceof JTextArea) {
            c.setBackground(LOG_BG);
            c.setForeground(CONSOLE_FG);
        }

        if (c instanceof JTextComponent tc) {
            styleTextComponent(tc);
        }

        if (c instanceof HudButton || c instanceof HudToggleButton) {
            // HUD buttons own their border and paint state.
        } else if (c instanceof JButton b) {
            styleButton(b);
        }

        if (c instanceof JCheckBox cb) {
            styleCheckBox(cb);
        }

        if (c instanceof JComboBox<?> comboBox) {
            styleComboBox(comboBox);
        }

        if (c instanceof JTable table) {
            styleTable(table);
        }

        if (c instanceof JSlider slider) {
            styleSlider(slider);
        }

        if (c instanceof JTabbedPane tp) {
            tp.setBackground(BG);
            tp.setForeground(FG);
            tp.setOpaque(true);
        }

        if (c instanceof JScrollPane sp) {
            styleScrollPane(sp);
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

    // -- GridBagLayout helpers -------------------------------------------------

    public static GridBagConstraints baseGbc() {
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

    public static void nextRow(GridBagConstraints gbc) {
        gbc.gridy++;
    }

    public static void addLabel(JPanel panel, String text, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(220, 42));
        panel.add(label, gbc);
    }

    public static void addLabel(JPanel panel, String text, GridBagConstraints gbc, int col, double weightX) {
        gbc.gridx = col;
        gbc.weightx = weightX;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel comp = new JLabel(text);
        comp.setPreferredSize(new Dimension(0, comp.getPreferredSize().height));
        panel.add(comp, gbc);
    }

    public static void addField(JPanel panel, JComponent comp, GridBagConstraints gbc, int col, double weightX) {
        gbc.gridx = col;
        gbc.weightx = weightX;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        comp.setPreferredSize(new Dimension(0, comp.getPreferredSize().height));
        panel.add(comp, gbc);
    }

    public static void addCheck(JPanel panel, JCheckBox check, GridBagConstraints gbc) {
        gbc.gridx = 2;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(check, gbc);
    }

    public static void addNestedPanel(JPanel parent, JPanel child, String title) {
        parent.add(new JLabel(title));
        parent.add(child);
    }

    public static void bindLock(JCheckBox lockCheck, JComponent field) {
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

}

package elite.intel.ui.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Shared colors, component factories, and layout helpers for the dark UI theme.
 */
public class AppTheme {
    /** Client-property key: set to {@link Boolean#TRUE} on a {@link javax.swing.JComponent} to prevent
     *  {@link #applyDarkPalette} from overriding its foreground colour. */
    public static final String HUD_LOCKED_FOREGROUND = "eliteIntel.hud.lockedForeground";
    static final String HUD_TABLE_STYLE_LOCKED = "eliteIntel.hud.tableStyleLocked";
    public static final String HUD_CARD_BORDER_COLOR = "eliteIntel.hud.cardBorderColor";

    public static final Color BG = new Color(0x151519);
    public static final Color LOG_BG = new Color(0x171927);
    public static final Color BG_PANEL = new Color(0x1F2032);
    public static final Color FG = new Color(0xE6E6E6);
    public static final Color BUTTON_FG = new Color(0xFFFFFF);
    public static final Color BUTTON_BG = new Color(0x03529F);
    public static final Color FG_MUTED = new Color(0x9A6A3C);
    public static final Color ACCENT = new Color(0xFF7100);
    public static final Color ACCENT_CYAN = new Color(0x33D7E8);
    public static final Color CONSOLE_FG = new Color(0xE0FFEF);
    public static final Color SEL_BG = new Color(0xE0FFEF);
    public static final Color SEL_FG = new Color(0x13181D);
    public static final Color TAB_UNSELECTED = new Color(0x151519);
    public static final Color TAB_SELECTED = new Color(0x151519);
    public static final Color DISABLED_FG = new Color(0x8B0101);

    // -- HUD design tokens -----------------------------------------------------

    public static final Color HUD_BG = new Color(0x090D12);
    public static final Color HUD_SHELL_BACKGROUND = HUD_BG;
    public static final Color HUD_CONTENT_BACKGROUND = HUD_BG;
    public static final Color HUD_PANEL_BG = new Color(0x101721);
    public static final Color HUD_PANEL_BG_ALT = new Color(0x151E2B);
    public static final Color HUD_BORDER = new Color(0x2D5C66);
    public static final Color HUD_BORDER_DIM = new Color(0x24313A);
    public static final Color HUD_ORANGE_SOFT = new Color(0xB85A14);
    public static final Color HUD_ORANGE_FILL = new Color(0x3A1E0A);
    public static final Color HUD_ORANGE_FILL_HOVER = new Color(0x532A0D);
    public static final Color HUD_CYAN = new Color(0x33D7E8);
    public static final Color HUD_CYAN_SOFT = new Color(0x49AFC7);
    public static final Color HUD_OK = new Color(0x4FC56B);
    public static final Color HUD_WARN = new Color(0xFFB000);
    public static final Color HUD_WARN_BG = new Color(0x2A2114);
    public static final Color HUD_DANGER = new Color(0xD94F4F);
    public static final Color HUD_DISABLED = new Color(0x6E4A28);
    public static final Color HUD_ROW_ALT = new Color(0x0E1420);
    public static final Color HUD_HOVER = new Color(0x182838);

    /** Muted amber/warm-orange body text for USER_INPUT log readouts. */
    public static final Color HUD_USER_INPUT_TEXT = new Color(0xBB7A32);
    /** Soft blue-grey body text for AI_RESPONSE log readouts. */
    public static final Color HUD_AI_RESPONSE_TEXT = new Color(0x72A2B4);
    /** Dim neutral grey body text for SYSTEM_LOG readout messages. */
    public static final Color HUD_SYSTEM_LOG_TEXT = new Color(0x5A6368);
    /** Subdued grey-blue for HH:mm:ss timestamp prefixes in SYSTEM_LOG entries. */
    public static final Color HUD_SYSTEM_LOG_TIMESTAMP = new Color(0x4A6270);

    public static final int HUD_GAP = 8;
    public static final int SHELL_GAP = 10;
    public static final int SCREEN_TOP_GAP = 12;
    public static final int HUD_PADDING = 10;
    public static final int HUD_PADDING_SMALL = 6;
    public static final int SUBTAB_CONTENT_GAP = HUD_GAP;
    public static final int HUD_BORDER_THICKNESS = 1;
    public static final int HUD_PANEL_ARC = 0;
    public static final int HUD_TOP_BAR_HEIGHT = 44;
    public static final int HUD_BADGE_HEIGHT = 20;
    public static final int HUD_FIELD_HEIGHT = 34;
    public static final int HUD_BUTTON_HEIGHT = 34;
    public static final int HUD_BUTTON_HEIGHT_COMPACT = 28;
    public static final int HUD_TABLE_ROW_HEIGHT = 34;
    public static final int HUD_TABLE_HEADER_HEIGHT = 30;
    public static final int HUD_TABLE_ROW_HEIGHT_COMPACT = 26;
    public static final int HUD_TABLE_HEADER_HEIGHT_COMPACT = 22;
    public static final int HUD_ICON_MAIN = 42;
    public static final int HUD_ICON_NAV = 24;
    public static final int HUD_ICON_SMALL = 28;
    public static final int HUD_FORM_ROW_HEIGHT_COMPACT = 22;
    public static final float HUD_FONT_XS      = 11f;
    public static final float HUD_FONT_SM      = 12f;
    public static final float HUD_FONT_MD      = 14f;
    public static final float HUD_FONT_LG      = 16f;
    /** @deprecated Use {@link #HUD_FONT_SM}. */
    @Deprecated public static final float HUD_FONT_SECTION = HUD_FONT_SM;
    /** @deprecated Use {@link #HUD_FONT_MD}. */
    @Deprecated public static final float HUD_FONT_LABEL   = 13f;
    /** @deprecated Use {@link #HUD_FONT_XS}. */
    @Deprecated public static final float HUD_FONT_BADGE   = HUD_FONT_XS;

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
     * Creates a compact read-only HUD field for metadata values.
     */
    public static JTextField makeMetadataField() {
        return new HudMetadataField();
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
     * Creates a quiet border for nested HUD surfaces that should not add another visible frame.
     */
    public static Border hudFlatBorder() {
        return new EmptyBorder(HUD_PADDING_SMALL, HUD_PADDING_SMALL, HUD_PADDING_SMALL, HUD_PADDING_SMALL);
    }

    /**
     * Creates the compact outer spacing used between the main navigation and screen content.
     */
    public static Border hudScreenBorder() {
        return new EmptyBorder(SCREEN_TOP_GAP, SHELL_GAP, SHELL_GAP, SHELL_GAP);
    }

    /**
     * Creates a denser outer spacing for high-density screens that need more room for content.
     * Use instead of {@link #hudScreenBorder()} when the screen is content-heavy and the standard
     * screen gap feels too spacious.
     */
    public static Border hudDenseScreenBorder() {
        return new EmptyBorder(HUD_GAP, HUD_PADDING_SMALL, HUD_PADDING_SMALL, HUD_PADDING_SMALL);
    }

    /**
     * Creates the compact spacing between a screen sub-tab row and its first content surface.
     */
    public static Border hudSubtabContentBorder() {
        return new EmptyBorder(SUBTAB_CONTENT_GAP, 0, 0, 0);
    }

    /**
     * Creates a restrained structural border for compact HUD cards and major modules.
     */
    public static Border hudMajorPanelBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(HUD_BORDER_DIM, HUD_BORDER_THICKNESS, true),
                new EmptyBorder(4, 6, 5, 6)
        );
    }

    /**
     * Creates a subtle frame for dense table/data-plane surfaces.
     */
    public static Border hudDataPlaneBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(HUD_BORDER_DIM, HUD_BORDER_THICKNESS, true),
                new EmptyBorder(1, 1, 1, 1)
        );
    }

    /**
     * Creates a left/right/bottom border for a data table that sits directly below a
     * {@link HudSearchField.Variant#TABLE_FILTER_CONNECTED} filter bar.
     * The top edge is omitted because the filter bar provides the shared top border line.
     */
    public static Border hudConnectedScrollPaneBorder() {
        return BorderFactory.createMatteBorder(0, 1, 1, 1, HUD_BORDER);
    }

    /**
     * Creates a compact separator for bottom command/status strips.
     */
    public static Border hudFooterSeparatorBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, HUD_BORDER),
                new EmptyBorder(12, 0, 6, 0)
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
     * Creates a muted cyan group-separator label for list sections within a data panel.
     * Use for group/category titles inside scrollable lists, not for screen-level section titles.
     */
    public static JLabel hudGroupLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(HUD_CYAN);
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
        if (tc instanceof JComponent jc
                && Boolean.TRUE.equals(jc.getClientProperty(HudSearchField.HUD_SEARCH_INNER_FIELD))) {
            styleSearchInnerField(tc);
            return;
        }
        if (tc instanceof HudMetadataField) {
            styleMetadataField(tc);
            return;
        }
        tc.setBackground(HUD_PANEL_BG_ALT);
        tc.setForeground(FG);
        tc.setCaretColor(HUD_CYAN);
        tc.setSelectionColor(HUD_CYAN);
        tc.setSelectedTextColor(SEL_FG);
        tc.setBorder(hudFieldBorder());
    }

    /**
     * Applies borderless text styling for fields embedded inside composite HUD controls.
     * Uses {@link #HUD_FONT_SM} so search/filter inputs read as secondary controls, not headings.
     */
    public static void styleSearchInnerField(JTextComponent tc) {
        tc.setOpaque(false);
        tc.setBackground(HUD_PANEL_BG_ALT);
        tc.setForeground(FG);
        tc.setCaretColor(HUD_CYAN);
        tc.setSelectionColor(HUD_CYAN);
        tc.setSelectedTextColor(SEL_FG);
        tc.setBorder(BorderFactory.createEmptyBorder());
        tc.setFont(tc.getFont().deriveFont(HUD_FONT_SM));
    }

    /**
     * Applies the compact borderless HUD treatment for read-only metadata fields.
     */
    public static void styleMetadataField(JTextComponent tc) {
        tc.setOpaque(true);
        tc.setBackground(HUD_PANEL_BG_ALT);
        tc.setForeground(FG);
        tc.setCaretColor(FG_MUTED);
        tc.setSelectionColor(HUD_CYAN);
        tc.setSelectedTextColor(SEL_FG);
        tc.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
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
        styleScrollBar(scrollPane.getVerticalScrollBar());
        styleScrollBar(scrollPane.getHorizontalScrollBar());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }

    private static void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setPreferredSize(new Dimension(9, 9));
        scrollBar.setBackground(HUD_BG);
        scrollBar.setUnitIncrement(16);
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = HUD_BORDER;
                thumbDarkShadowColor = HUD_BORDER_DIM;
                thumbHighlightColor = HUD_CYAN;
                trackColor = HUD_PANEL_BG;
                trackHighlightColor = HUD_PANEL_BG_ALT;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return zeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return zeroButton();
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(HUD_BORDER);
                    g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
                            Math.max(1, thumbBounds.width - 2),
                            Math.max(1, thumbBounds.height - 2), 8, 8);
                    g2.setColor(HUD_CYAN);
                    if (scrollbar.getOrientation() == Adjustable.VERTICAL) {
                        g2.drawLine(thumbBounds.x + thumbBounds.width - 2,
                                thumbBounds.y + 4,
                                thumbBounds.x + thumbBounds.width - 2,
                                thumbBounds.y + thumbBounds.height - 5);
                    } else {
                        g2.drawLine(thumbBounds.x + 4,
                                thumbBounds.y + thumbBounds.height - 2,
                                thumbBounds.x + thumbBounds.width - 5,
                                thumbBounds.y + thumbBounds.height - 2);
                    }
                } finally {
                    g2.dispose();
                }
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(HUD_BG);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }

            private JButton zeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
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

    /** @deprecated Use {@link #makeStandardTabs()} or {@code new HudTabbedPane(HudTabbedPane.Level.STANDARD)}. */
    @Deprecated
    public static void styleTabbedPane(JTabbedPane tp) {
        HudTabbedPane.applyStyle(tp, HudTabbedPane.Level.STANDARD);
    }

    /** @deprecated Use {@link #makeSectionTabs()} or {@code new HudTabbedPane(HudTabbedPane.Level.SECTION)}. */
    @Deprecated
    public static void styleFlatTabbedPane(JTabbedPane tp) {
        HudTabbedPane.applyStyle(tp, HudTabbedPane.Level.SECTION);
    }

    /** @deprecated Use {@link #makeSectionTabs()} or {@code new HudTabbedPane(HudTabbedPane.Level.SECTION)}. */
    @Deprecated
    public static void styleCompactFlatTabbedPane(JTabbedPane tp) {
        HudTabbedPane.applyStyle(tp, HudTabbedPane.Level.SECTION);
    }

    /** @deprecated Use {@link #makeMainNavTabs()} or {@code new HudTabbedPane(HudTabbedPane.Level.MAIN_NAV)}. */
    @Deprecated
    public static void styleMainNavigationTabbedPane(JTabbedPane tp) {
        HudTabbedPane.applyStyle(tp, HudTabbedPane.Level.MAIN_NAV);
    }

    /** Creates a HUD-styled application-level navigation tabbed pane. */
    public static JTabbedPane makeMainNavTabs() {
        return new HudTabbedPane(HudTabbedPane.Level.MAIN_NAV);
    }

    /** Creates a HUD-styled sub-navigation tabbed pane for screen sections. */
    public static JTabbedPane makeSectionTabs() {
        return new HudTabbedPane(HudTabbedPane.Level.SECTION);
    }

    /** Creates a HUD-styled compact bold tabbed pane for data panels. */
    public static JTabbedPane makeCompactTabs() {
        return new HudTabbedPane(HudTabbedPane.Level.COMPACT);
    }

    /** Creates a HUD-styled settings-style tabbed pane with a visible content frame. */
    public static JTabbedPane makeStandardTabs() {
        return new HudTabbedPane(HudTabbedPane.Level.STANDARD);
    }

    // -- Dark palette ----------------------------------------------------------

    public static void applyDarkPalette(Component c) {
        if (c == null) return;
        boolean lockForeground = c instanceof JComponent jc
                && Boolean.TRUE.equals(jc.getClientProperty(HUD_LOCKED_FOREGROUND));

        if (c instanceof TopStatusBar || c instanceof HudPanel || c instanceof StatusBadge) {
            // HUD primitives own their painting and state colours.
        } else if (c instanceof JPanel || c instanceof JTabbedPane || c instanceof JScrollPane) {
            c.setBackground(HUD_CONTENT_BACKGROUND);
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

        boolean searchClearButton = c instanceof JComponent jc
                && Boolean.TRUE.equals(jc.getClientProperty(HudSearchField.HUD_SEARCH_CLEAR_BUTTON));

        if (c instanceof HudButton || c instanceof HudToggleButton || searchClearButton) {
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

        if (c instanceof JTable table
                && !Boolean.TRUE.equals(table.getClientProperty(HUD_TABLE_STYLE_LOCKED))) {
            styleTable(table);
        }

        if (c instanceof JSlider slider) {
            styleSlider(slider);
        }

        if (c instanceof JTabbedPane tp) {
            tp.setBackground(HUD_CONTENT_BACKGROUND);
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

        // TopStatusBar owns all colours of its children — do not recurse into it.
        if (c instanceof Container cont && !(c instanceof TopStatusBar)) {
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

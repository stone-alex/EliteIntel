package elite.intel.ui.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Placeholder panel for future user-defined command macro editing.
 */
public class MacrosTabPanel extends JPanel {

    public MacrosTabPanel() {
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setBackground(AppTheme.BG);

        add(headerPanel(), BorderLayout.NORTH);
        add(emptyStatePanel(), BorderLayout.CENTER);
    }

    private JPanel headerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel(getText("actions.macros.title"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 4f));
        title.setForeground(AppTheme.FG);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(getText("actions.macros.description"));
        description.setForeground(AppTheme.FG_MUTED);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(description);
        return panel;
    }

    private JPanel emptyStatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.ACCENT, 1, true),
                new EmptyBorder(32, 32, 32, 32)
        ));

        JLabel text = new JLabel(getText("actions.macros.emptyState"));
        text.setForeground(AppTheme.FG_MUTED);
        text.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(text);
        return panel;
    }
}

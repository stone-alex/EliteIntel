package elite.intel.ui.view;

import javax.swing.*;
import java.awt.*;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

/**
 * Groups bindings, command catalog, and future macros under one Actions tab.
 */
public class ActionsTabPanel extends JPanel {

    private final BindingsTabPanel bindingsTabPanel = new BindingsTabPanel();
    private final CommandCatalogTablePanel commandCatalogTablePanel = new CommandCatalogTablePanel();
    private final MacrosTabPanel macrosTabPanel = new MacrosTabPanel();

    public ActionsTabPanel() {
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.TOP);
        AppTheme.styleTabbedPane(tabs);
        tabs.addTab(getText("actions.tab.bindings"), bindingsTabPanel);
        tabs.addTab(getText("actions.tab.commands"), commandCatalogTablePanel);
        tabs.addTab(getText("actions.tab.macros"), macrosTabPanel);

        add(tabs, BorderLayout.CENTER);
    }

    public void initData() {
        bindingsTabPanel.initData();
        commandCatalogTablePanel.initData();
        macrosTabPanel.initData();
    }

    public void dispose() {
        bindingsTabPanel.dispose();
    }
}

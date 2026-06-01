package elite.intel.ui.view.settings;

import elite.intel.db.dao.ShipSettingsDao;
import elite.intel.db.managers.ShipSettingsManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

public class ShipSettingsPopup {

    public static SettingsPopup create(Component parent, String identifier, ShipSettingsDao.ShipSettings shipSettings) {
        List<SettingRow> rows = new ArrayList<>();
        rows.add(new HonkSystemSettingsPanel(
                getText("automation.honkSystemOnEntry"),
                shipSettings::isHonkOnJump,
                shipSettings::setHonkOnJump,

                List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"),
                shipSettings::getHonkFireGroup,
                shipSettings::setHonkFireGroup,

                List.of(1, 2),
                shipSettings::getHonkTrigger,
                shipSettings::setHonkTrigger
        ));

        String title = identifier != null ? getText("popup.shipSettings", identifier) : getText("popup.shipSettings.default");
        return new SettingsPopup(parent, title, rows,
                () -> ShipSettingsManager.getInstance().saveShipSettings(shipSettings)
        );
    }
}

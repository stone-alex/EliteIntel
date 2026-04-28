package elite.intel.ui.view.settings;

import elite.intel.db.dao.ShipSettingsDao;
import elite.intel.db.managers.ShipSettingsManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ShipSettingsPopup {

    public static SettingsPopup create(Component parent, String identifier, ShipSettingsDao.ShipSettings shipSettings) {
        List<SettingRow> rows = new ArrayList<>();
        rows.add(new HonkSystemSettingsPanel(
                "Honk system on entry",
                shipSettings::isHonkOnJump,
                shipSettings::setHonkOnJump,

                List.of("A", "B", "C", "D", "E"),
                shipSettings::getHonkFireGroup,
                shipSettings::setHonkFireGroup,

                List.of(1, 2),
                shipSettings::getHonkTrigger,
                shipSettings::setHonkTrigger
        ));

        String title = identifier != null ? identifier + " Settings" : "Ship Settings";
        return new SettingsPopup(parent, title, rows,
                () -> ShipSettingsManager.getInstance().saveShipSettings(shipSettings)
        );
    }
}

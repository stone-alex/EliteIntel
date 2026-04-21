package elite.intel.ui.view.settings;

import elite.intel.db.managers.GlobalSettingsManager;

import java.awt.*;
import java.util.List;

public class GlobalSettingsPopup {

    public static SettingsPopup create(Component parent) {
        GlobalSettingsManager mgr = GlobalSettingsManager.getInstance();
        List<SettingRow> rows = List.of(
                new CheckboxRow("Auto speed up for FTL", mgr::getAutoSpeedUpForFtl, mgr::setAutoSpeedUpForFtl),
                new CheckboxRow("Auto lights off for FTL", mgr::getAutoLightsForFtl, mgr::setAutoLightsForFtl),
                new CheckboxRow("Auto night vision off for FTL", mgr::getAutoNightVisionOffForSrv, mgr::setAutoNightVisionOffForSrv),
                new CheckboxRow("Auto hardpoints retract for FTL", mgr::getAutoHardpointsRetractForFtl, mgr::setAutoHardpointsRetractForFtl),
                new CheckboxRow("Auto landing gear up for FTL", mgr::getAutoLandingGearUpForFtl, mgr::setAutoLandingGearUpForFtl),
                new CheckboxRow("Auto cargo scoop retract for FTL", mgr::getAutoCargoScoopRetractForFtl, mgr::setAutoCargoScoopRetractForFtl),
                new CheckboxRow("Auto gear up on take off", mgr::getAutoGearUpOnTakeOff, mgr::setAutoGearUpOnTakeOff),
                new CheckboxRow("Auto exit UI before opening another panel", mgr::getAutoExitUiBeforeOpeningAnotherWindow, mgr::setAutoExitUiBeforeOpeningAnotherWindow),
                new CheckboxRow("Auto lights off for SRV deployment", mgr::getAutoLightsOffForSrvDeployment, mgr::setAutoLightsOffForSrvDeployment),
                new CheckboxRow("Request fighter dock on FTL / cancel if out", mgr::getAutoFighterOutFighterDocking, mgr::setAutoFighterOutFighterDocking)
        );
        return new SettingsPopup(parent, "Ship Options", rows);
    }
}

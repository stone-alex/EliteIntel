package elite.intel.ui.view.settings;

import elite.intel.db.managers.GlobalSettingsManager;

import java.awt.*;
import java.util.List;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;

public class GlobalSettingsPopup {

    public static SettingsPopup create(Component parent) {
        GlobalSettingsManager mgr = GlobalSettingsManager.getInstance();
        List<SettingRow> rows = List.of(
                new CheckboxRow(getText("automation.autoSpeedUpForFtl"), mgr::getAutoSpeedUpForFtl, mgr::setAutoSpeedUpForFtl),
                new CheckboxRow(getText("automation.autoLightsOffForFtl"), mgr::getAutoLightsForFtl, mgr::setAutoLightsForFtl),
                new CheckboxRow(getText("automation.autoNightVisionOffForFtl"), mgr::getAutoNightVisionOff, mgr::setAutoNightVisionOffForSrv),
                new CheckboxRow(getText("automation.autoHardpointsRetractForFtl"), mgr::getAutoHardpointsRetractForFtl, mgr::setAutoHardpointsRetractForFtl),
                new CheckboxRow(getText("automation.autoLandingGearUpForFtl"), mgr::getAutoLandingGearUpForFtl, mgr::setAutoLandingGearUpForFtl),
                new CheckboxRow(getText("automation.autoCargoScoopRetractForFtl"), mgr::getAutoCargoScoopRetractForFtl, mgr::setAutoCargoScoopRetractForFtl),
                new CheckboxRow(getText("automation.autoGearUpOnTakeOff"), mgr::getAutoGearUpOnTakeOff, mgr::setAutoGearUpOnTakeOff),
                new CheckboxRow(getText("automation.autoExitUiBeforeOpeningAnotherPanel"), mgr::getAutoExitUiBeforeOpeningAnotherWindow, mgr::setAutoExitUiBeforeOpeningAnotherWindow),
                new CheckboxRow(getText("automation.autoLightsOffForSrvDeployment"), mgr::getAutoLightsOffForSrvDeployment, mgr::setAutoLightsOffForSrvDeployment),
                new CheckboxRow(getText("automation.requestFighterDockOnFtl"), mgr::getAutoFighterOutFighterDocking, mgr::setAutoFighterOutFighterDocking)
        );
        return new SettingsPopup(parent, getText("popup.shipOptions"), rows);
    }
}

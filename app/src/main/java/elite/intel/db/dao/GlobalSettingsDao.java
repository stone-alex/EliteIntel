package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(GlobalSettingsDao.GlobalSettingEntityMapper.class)
public interface GlobalSettingsDao {

    @SqlQuery("SELECT * FROM global_settings WHERE id = 1")
    GlobalSettingsDao.GlobalSettings get();

    @SqlUpdate("""
                    INSERT OR REPLACE INTO global_settings (id, autoSpeedUpForFtl,
                                                                autoLightsForFtl, 
                                                                autoNightVisionOffForSrv, 
                                                                autoCargoScoopRetractForFtl, 
                                                                autoLandingGearUpForFtl, 
                                                                autoHardpointsRetractForFtl, 
                                                                autoGearUpOnTakeOff, 
                                                                autoExitUiBeforeOpeningAnotherWindow, 
                                                                autoLightsOffForSrvDeployment, 
                                                                autoFighterOutFighterDocking) 
                    VALUES (1, :autoSpeedUpForFtl,
                                :autoLightsForFtl,
                                :autoNightVisionOffForSrv,
                                :autoCargoScoopRetractForFtl, 
                                :autoLandingGearUpForFtl, 
                                :autoHardpointsRetractForFtl, 
                                :autoGearUpOnTakeOff, 
                                :autoExitUiBeforeOpeningAnotherWindow, 
                                :autoLightsOffForSrvDeployment, 
                                :autoFighterOutFighterDocking)
            """)
    void save(@BindBean GlobalSettings settings);


    class GlobalSettingEntityMapper implements RowMapper<GlobalSettings> {
        @Override
        public GlobalSettings map(ResultSet rs, StatementContext ctx) throws SQLException {
            GlobalSettings entity = new GlobalSettings();
            entity.setAutoSpeedUpForFtl(rs.getBoolean("autoSpeedUpForFtl"));
            entity.setAutoLightsForFtl(rs.getBoolean("autoLightsForFtl"));
            entity.setAutoNightVisionOffForSrv(rs.getBoolean("autoNightVisionOffForSrv"));
            entity.setAutoCargoScoopRetractForFtl(rs.getBoolean("autoCargoScoopRetractForFtl"));
            entity.setAutoLandingGearUpForFtl(rs.getBoolean("autoLandingGearUpForFtl"));
            entity.setAutoHardpointsRetractForFtl(rs.getBoolean("autoHardpointsRetractForFtl"));
            entity.setAutoGearUpOnTakeOff(rs.getBoolean("autoGearUpOnTakeOff"));
            entity.setAutoExitUiBeforeOpeningAnotherWindow(rs.getBoolean("autoExitUiBeforeOpeningAnotherWindow"));
            entity.setAutoLightsOffForSrvDeployment(rs.getBoolean("autoLightsOffForSrvDeployment"));
            entity.setAutoFighterOutFighterDocking(rs.getBoolean("autoFighterOutFighterDocking"));
            return entity;
        }
    }


    class GlobalSettings {
        boolean autoSpeedUpForFtl;
        boolean autoLightsForFtl;
        boolean autoNightVisionOffForSrv;
        boolean autoCargoScoopRetractForFtl;
        boolean autoLandingGearUpForFtl;
        boolean autoHardpointsRetractForFtl;
        boolean autoGearUpOnTakeOff;
        boolean autoExitUiBeforeOpeningAnotherWindow;
        boolean autoLightsOffForSrvDeployment;
        boolean autoFighterOutFighterDocking;

        public boolean isAutoSpeedUpForFtl() {
            return autoSpeedUpForFtl;
        }

        public void setAutoSpeedUpForFtl(boolean autoSpeedUpForFtl) {
            this.autoSpeedUpForFtl = autoSpeedUpForFtl;
        }

        public boolean isAutoLightsForFtl() {
            return autoLightsForFtl;
        }

        public void setAutoLightsForFtl(boolean autoLightsForFtl) {
            this.autoLightsForFtl = autoLightsForFtl;
        }

        public boolean isAutoNightVisionOffForSrv() {
            return autoNightVisionOffForSrv;
        }

        public void setAutoNightVisionOffForSrv(boolean autoNightVisionOffForSrv) {
            this.autoNightVisionOffForSrv = autoNightVisionOffForSrv;
        }

        public boolean isAutoCargoScoopRetractForFtl() {
            return autoCargoScoopRetractForFtl;
        }

        public void setAutoCargoScoopRetractForFtl(boolean autoCargoScoopRetractForFtl) {
            this.autoCargoScoopRetractForFtl = autoCargoScoopRetractForFtl;
        }

        public boolean isAutoLandingGearUpForFtl() {
            return autoLandingGearUpForFtl;
        }

        public void setAutoLandingGearUpForFtl(boolean autoLandingGearUpForFtl) {
            this.autoLandingGearUpForFtl = autoLandingGearUpForFtl;
        }

        public boolean isAutoHardpointsRetractForFtl() {
            return autoHardpointsRetractForFtl;
        }

        public void setAutoHardpointsRetractForFtl(boolean autoHardpointsRetractForFtl) {
            this.autoHardpointsRetractForFtl = autoHardpointsRetractForFtl;
        }

        public boolean isAutoGearUpOnTakeOff() {
            return autoGearUpOnTakeOff;
        }

        public void setAutoGearUpOnTakeOff(boolean autoGearUpOnTakeOff) {
            this.autoGearUpOnTakeOff = autoGearUpOnTakeOff;
        }

        public boolean isAutoExitUiBeforeOpeningAnotherWindow() {
            return autoExitUiBeforeOpeningAnotherWindow;
        }

        public void setAutoExitUiBeforeOpeningAnotherWindow(boolean autoExitUiBeforeOpeningAnotherWindow) {
            this.autoExitUiBeforeOpeningAnotherWindow = autoExitUiBeforeOpeningAnotherWindow;
        }

        public boolean isAutoLightsOffForSrvDeployment() {
            return autoLightsOffForSrvDeployment;
        }

        public void setAutoLightsOffForSrvDeployment(boolean autoLightsOffForSrvDeployment) {
            this.autoLightsOffForSrvDeployment = autoLightsOffForSrvDeployment;
        }

        public boolean isAutoFighterOutFighterDocking() {
            return autoFighterOutFighterDocking;
        }

        public void setAutoFighterOutFighterDocking(boolean autoFighterOutFighterDocking) {
            this.autoFighterOutFighterDocking = autoFighterOutFighterDocking;
        }
    }
}

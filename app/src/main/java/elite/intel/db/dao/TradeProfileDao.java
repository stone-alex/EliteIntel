package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterRowMapper(TradeProfileDao.TradeProfileMapper.class)
public interface TradeProfileDao {

    @SqlQuery("SELECT * FROM trade_profile where shipId= :shipId")
    TradeProfile getTradeProfile(int shipId);

    @SqlUpdate("""
            INSERT INTO trade_profile (shipId, padSize, allowPlanetary, allowProhibited, allowPermit, allowFleetCarrier, defaultBudget, maxDistanceLy, maxHops)
            values(:shipId, :padSize, :allowPlanetary, :allowProhibited, :allowPermit, :allowFleetCarrier, :defaultBudget, :maxDistanceLy, :maxHops)
                on conflict do update set
                    padSize = excluded.padSize,
                    allowFleetCarrier = excluded.allowFleetCarrier,
                    allowPlanetary = excluded.allowPlanetary,
                    allowPermit = excluded.allowPermit,
                    allowProhibited = excluded.allowProhibited,
                    defaultBudget = excluded.defaultBudget,
                    maxDistanceLy = excluded.maxDistanceLy,
                    maxHops = excluded.maxHops
            """)
    void save(@BindBean TradeProfileDao.TradeProfile profile);




    class TradeProfileMapper implements RowMapper<TradeProfile> {

        @Override public TradeProfile map(ResultSet rs, StatementContext ctx) throws SQLException {
            TradeProfile profile = new TradeProfile();
            profile.setShipId(rs.getInt("shipId"));
            profile.setPadSize(rs.getString("padSize"));
            profile.setAllowPlanetary(rs.getBoolean("allowPlanetary"));
            profile.setAllowProhibited(rs.getBoolean("allowProhibited"));
            profile.setAllowPermit(rs.getBoolean("allowPermit"));
            profile.setAllowFleetCarrier(rs.getBoolean("allowFleetCarrier"));
            profile.setDefaultBudget(rs.getDouble("defaultBudget"));
            profile.setMaxDistanceLy(rs.getInt("maxDistanceLy"));
            profile.setMaxHops(rs.getInt("maxHops"));
            return profile;
        }
    }



    class TradeProfile {
        private Integer shipId;
        private String padSize;
        private boolean allowPlanetary;
        private boolean allowProhibited;
        private boolean allowPermit;
        private boolean allowFleetCarrier;
        private Double defaultBudget;
        private Integer maxDistanceLy;
        private Integer maxHops;

        public Integer getShipId() {
            return shipId;
        }

        public void setShipId(Integer shipId) {
            this.shipId = shipId;
        }

        public String getPadSize() {
            return padSize;
        }

        public void setPadSize(String padSize) {
            this.padSize = padSize;
        }

        public boolean isAllowPlanetary() {
            return allowPlanetary;
        }

        public void setAllowPlanetary(boolean allowPlanetary) {
            this.allowPlanetary = allowPlanetary;
        }

        public boolean isAllowProhibited() {
            return allowProhibited;
        }

        public void setAllowProhibited(boolean allowProhibited) {
            this.allowProhibited = allowProhibited;
        }

        public boolean isAllowPermit() {
            return allowPermit;
        }

        public void setAllowPermit(boolean allowPermit) {
            this.allowPermit = allowPermit;
        }

        public boolean isAllowFleetCarrier() {
            return allowFleetCarrier;
        }

        public void setAllowFleetCarrier(boolean allowFleetCarrier) {
            this.allowFleetCarrier = allowFleetCarrier;
        }

        public Double getDefaultBudget() {
            return defaultBudget;
        }

        public void setDefaultBudget(Double defaultBudget) {
            this.defaultBudget = defaultBudget;
        }

        public Integer getMaxDistanceLy() {
            return maxDistanceLy;
        }

        public void setMaxDistanceLy(Integer maxDistanceLy) {
            this.maxDistanceLy = maxDistanceLy;
        }

        public Integer getMaxHops() {
            return maxHops;
        }

        public void setMaxHops(Integer maxHops) {
            this.maxHops = maxHops;
        }
    }
}
